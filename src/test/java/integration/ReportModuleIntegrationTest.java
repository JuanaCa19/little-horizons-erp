package integration;

import dao.impl.ReportDAOImpl;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import model.enums.*;
import model.report.*;
import service.ReportService;
import util.exception.ValidationException;

public class ReportModuleIntegrationTest {
    private static int checks;
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message);checks++; }
    private static void rejected(Runnable work) { try { work.run(); } catch(ValidationException e) { checks++;return; }throw new AssertionError("Expected validation failure"); }
    public static void main(String[] args) throws Exception {
        LocalDate today=LocalDate.of(2026,11,16),from=LocalDate.of(2026,10,19),to=LocalDate.of(2026,10,25);
        try(TemporaryDatabase db=new TemporaryDatabase("grados","estudiantes","profesores","matriculas","materias","asignaciones_profesor","horarios_asignacion","inscripciones_materia","registros_academicos","asistencias","conceptos_pago","pagos","cargos_estudiante","clubes","membresias_club","eventos","participantes_evento","categorias_recurso","recursos","prestamos_estudiante","prestamos_profesor")) {
            int[] queries={0};
            Supplier<Connection> readOnly=() -> {
                Connection delegate=db.connections().get();
                return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,values) -> {
                    if(method.getName().equals("prepareStatement")) {
                        String sql=((String)values[0]).strip().toUpperCase(Locale.ROOT);
                        if(!sql.startsWith("SELECT ") || sql.contains("FOR UPDATE")) throw new AssertionError("Reports must be read-only and non-locking: "+sql);
                        queries[0]++;
                    }
                    if(method.getName().equals("createStatement")) throw new AssertionError("Reports must use prepared statements");
                    try { return method.invoke(delegate,values); } catch(InvocationTargetException e) { throw e.getCause(); }
                });
            };
            ReportService service=new ReportService(new ReportDAOImpl(),readOnly);
            SchoolOverview empty=service.overview(today);
            check(empty.totalStudents()==0 && empty.openLoans()==0 && empty.availableResources()==0,"Empty school totals are zero");
            check(service.studentAverages(2026,AcademicPeriod.FIRST).isEmpty() && service.income(from,to).isEmpty(),"Empty averages and income have no fabricated rows");
            db.execute("INSERT INTO grados(id_grado,nombre_grado,nivel_numerico) VALUES(1,'Primero',1),(2,'Segundo',2),(3,'Tercero',3)");
            db.execute("INSERT INTO estudiantes(id_estudiante,tipo_documento,numero_documento,primer_nombre,primer_apellido,fecha_nacimiento,genero,fecha_ingreso,estado) VALUES(1,'TI','111','Ana','Perez','2010-01-01','F','2020-01-01','Activo'),(2,'TI','222','Luis','Ruiz','2010-01-01','M','2020-01-01','Activo'),(3,'TI','333','Sara','Diaz','2010-01-01','F','2020-01-01','Retirado')");
            db.execute("INSERT INTO profesores(id_profesor,tipo_documento,numero_documento,primer_nombre,primer_apellido,correo_electronico,fecha_contratacion,estado) VALUES(1,'CC','100','Maria','Gil','maria@example.com','2020-01-01','Activo'),(2,'CC','200','Jose','Diaz','jose@example.com','2020-01-01','Inactivo')");
            db.execute("INSERT INTO matriculas(id_estudiante,id_grado,anio_academico,estado_matricula,fecha_matricula) VALUES(1,1,2026,'Activa','2026-01-15'),(2,1,2026,'Completada','2026-01-15'),(3,2,2026,'Retirada','2026-01-15')");
            db.execute("INSERT INTO materias(id_materia,codigo_materia,nombre_materia,id_grado) VALUES(1,'MAT-1','Matematicas',1),(2,'ESP-1','Castellano',1)");
            db.execute("INSERT INTO asignaciones_profesor(id_asignacion,id_profesor,id_materia,anio_academico,seccion) VALUES(1,1,1,2026,'A'),(2,1,2,2026,'A')");
            db.execute("INSERT INTO horarios_asignacion(id_asignacion,dia_semana,hora_inicio,hora_fin) VALUES(1,'Lunes','07:00','08:00'),(1,'Martes','07:00','08:00'),(2,'Jueves','09:00','09:30')");
            db.execute("INSERT INTO inscripciones_materia(id_estudiante,id_materia,anio_academico,periodo_academico,seccion,fecha_inscripcion) VALUES(1,1,2026,'I','A','2026-01-20'),(2,1,2026,'I','B','2026-01-20'),(1,2,2026,'I','A','2026-01-20'),(1,1,2026,'IV','A','2026-09-20')");
            db.execute("INSERT INTO registros_academicos(id_estudiante,id_materia,id_profesor,anio_academico,periodo_academico,valor_nota,fecha_registro) VALUES(1,1,1,2026,'I',4.0,'2026-04-01'),(1,2,1,2026,'I',2.0,'2026-04-01'),(2,1,1,2026,'I',5.0,'2026-04-01'),(1,1,1,2026,'Final',4.8,'2026-11-01'),(1,1,1,2025,'I',1.0,'2025-04-01')");
            db.execute("INSERT INTO asistencias(id_estudiante,id_materia,fecha_asistencia,estado_asistencia) VALUES(1,1,'2026-10-19','Presente'),(1,1,'2026-10-20','Ausente'),(1,2,'2026-10-20','Ausente'),(1,1,'2026-10-21','Tardanza'),(1,1,'2026-10-22','Excusa M\u00e9dica'),(1,1,'2026-10-23','Ausente'),(1,1,'2026-10-24','Ausente'),(1,1,'2026-10-25','Ausente'),(2,1,'2026-10-19','Excusa M\u00e9dica'),(2,1,'2026-10-18','Ausente')");
            db.execute("INSERT INTO conceptos_pago(id_concepto,codigo_concepto,nombre_concepto,monto_base,porcentaje_iva) VALUES(1,'UNIFORM','Uniforme',100,19),(2,'PENSION','Pension',10,0)");
            db.execute("INSERT INTO pagos(id_pago,id_estudiante,id_acudiente,id_concepto,monto_pagado,fecha_pago,metodo_pago,numero_recibo,anio_academico,periodo_academico) VALUES(1,1,1,1,119,'2026-10-19','Efectivo','R-1',2026,'I'),(2,2,1,1,238,'2026-10-25','Transferencia','R-2',2026,'I'),(3,1,1,1,500,'2026-10-18','Efectivo','R-3',2026,'I')");
            db.execute("INSERT INTO cargos_estudiante(id_estudiante,id_concepto,monto_cargo,anio_academico,periodo_academico,mes_cargo,fecha_vencimiento,estado,id_pago) VALUES(1,1,100,2026,'I',1,'2026-10-15','Pendiente',NULL),(1,1,100,2026,'I',2,'2026-10-16','Pendiente',NULL),(1,1,50,2026,'I',3,'2026-11-17','Pendiente',NULL),(1,1,0,2026,'I',4,'2026-09-01','Pendiente',NULL),(1,1,900,2026,'I',5,'2026-09-01','Anulado',NULL),(1,1,100,2026,'I',6,'2026-09-01','Pagado',1),(2,2,10,2026,'I',1,'2026-11-16','Pendiente',NULL)");
            db.execute("INSERT INTO clubes(id_club,nombre_club,capacidad_maxima,esta_activo) VALUES(1,'Robotica',3,1),(2,'Arte',10,0)");
            db.execute("INSERT INTO membresias_club(id_estudiante,id_club,fecha_ingreso,estado_membresia,fecha_retiro) VALUES(1,1,'2026-02-01','Retirada','2026-03-01'),(1,1,'2026-04-01','Activa',NULL),(2,1,'2026-04-01','Activa',NULL)");
            db.execute("INSERT INTO eventos(id_evento,nombre_evento,tipo_evento,fecha_evento,tiene_competencia) VALUES(1,'Feria','Cient\u00edfico','2026-10-19',1),(2,'Dia cultural','Cultural','2026-10-25',0)");
            db.execute("INSERT INTO participantes_evento(id_evento,id_estudiante,rol_participacion,posicion_obtenida) VALUES(1,1,'Competidor','1er Lugar'),(1,2,'Asistente',NULL)");
            db.execute("INSERT INTO categorias_recurso(id_categoria,nombre_categoria) VALUES(1,'Libros'),(2,'Equipos'),(3,'Sin recursos')");
            db.execute("INSERT INTO recursos(id_recurso,codigo_recurso,nombre_recurso,id_categoria,estado_recurso) VALUES(1,'LIB-1','Libro Uno',1,'Prestado'),(2,'LIB-2','Libro Dos',1,'Prestado'),(3,'EQ-1','Microscopio',2,'En Mantenimiento'),(4,'LIB-3','Libro Tres',1,'Disponible')");
            db.execute("INSERT INTO prestamos_estudiante(id_prestamo,id_recurso,id_estudiante,fecha_prestamo,fecha_devolucion_esperada,estado_prestamo) VALUES(1,1,1,'2026-11-01','2026-11-14','Activo')");
            db.execute("INSERT INTO prestamos_profesor(id_prestamo,id_recurso,id_profesor,fecha_prestamo,fecha_devolucion_esperada,fecha_devolucion_real,estado_prestamo) VALUES(1,2,1,'2026-11-01','2026-11-16',NULL,'Activo'),(2,3,1,'2026-11-01','2026-11-10','2026-11-16','Devuelto')");
            SchoolOverview overview=service.overview(today);
            check(overview.totalStudents()==3 && overview.activeStudents()==2 && overview.activeTeachers()==1 && overview.activeClubs()==1,"School population overview");
            check(overview.totalResources()==4 && overview.availableResources()==1 && overview.openLoans()==2 && overview.overdueLoans()==1,"Resource overview includes both borrower types and excludes returns");
            var enrollment=service.enrollmentByGrade(2026);
            check(enrollment.size()==3 && enrollment.get(0).total()==2 && enrollment.get(0).active()==1 && enrollment.get(0).completed()==1 && enrollment.get(1).withdrawn()==1 && enrollment.get(2).total()==0,"Enrollment statuses and empty grades");
            var averages=service.studentAverages(2026,AcademicPeriod.FIRST);
            check(averages.size()==2 && averages.getFirst().studentId()==2 && averages.get(1).average().compareTo(new BigDecimal("3.00"))==0 && averages.get(1).gradedSubjects()==2,"Averages exclude ungraded students Final and other years");
            check(service.studentsBelowAverage(2026,AcademicPeriod.FIRST,new BigDecimal("3.00")).isEmpty(),"Support threshold is strictly below");
            check(service.studentsBelowAverage(2026,AcademicPeriod.FIRST,new BigDecimal("3.01")).size()==1,"Support students filtered by threshold");
            check(service.studentAverages(2026,AcademicPeriod.FINAL).getFirst().average().compareTo(new BigDecimal("4.80"))==0,"Final is a separate period");
            var subjects=service.subjectAverages(2026,AcademicPeriod.FIRST);
            check(subjects.size()==3 && subjects.stream().filter(value -> value.subjectId()==1).allMatch(value -> value.gradedStudents()==1),"Subject averages grouped by section without duplication");
            check(service.subjectAverages(2026,AcademicPeriod.FINAL).getFirst().section().equals("A"),"Final uses fourth-period section");
            var workload=service.teacherWorkload(2026);
            check(workload.size()==2 && workload.getFirst().assignments()==2 && workload.getFirst().scheduledMinutes()==150 && workload.getFirst().scheduledHours().compareTo(new BigDecimal("2.50"))==0,"Several schedules do not inflate assignment count");
            check(workload.get(1).assignments()==0 && workload.get(1).scheduledMinutes()==0,"Teachers without assignments remain visible");
            var attendance=service.attendance(from,LocalDate.of(2026,10,22));
            var ana=attendance.stream().filter(value -> value.studentId()==1).findFirst().orElseThrow();
            check(ana.total()==5 && ana.present()==1 && ana.absent()==2 && ana.late()==1 && ana.excused()==1 && ana.attendanceRate().compareTo(new BigDecimal("50.00"))==0,"Attendance records and rate denominator");
            check(attendance.stream().filter(value -> value.studentId()==2).findFirst().orElseThrow().attendanceRate()==null,"Medical-excuse-only attendance has no fabricated percentage");
            check(service.weeklyAbsenceAlerts(from,3).size()==1 && service.weeklyAbsenceAlerts(from,3).getFirst().absenceDays()==4,"Weekly alerts count distinct dates across subjects");
            check(service.weeklyAbsenceAlerts(from,4).isEmpty(),"Weekly alert threshold is strict");
            var income=service.income(from,to);
            check(income.size()==1 && income.getFirst().payments()==2 && income.getFirst().total().compareTo(new BigDecimal("357.00"))==0,"Income uses actual totals including VAT and inclusive date boundaries");
            var debts=service.debts(today,false);DebtReport debt=debts.stream().filter(value -> value.studentId()==1).findFirst().orElseThrow();
            check(debts.size()==2 && debt.pendingCharges()==3 && debt.pendingBase().compareTo(new BigDecimal("250.00"))==0 && debt.overdueCharges()==1 && debt.overdueBase().compareTo(new BigDecimal("100.00"))==0,"Debt excludes zero paid and cancelled charges; exact month boundary is not overdue");
            check(service.debts(today,true).size()==1 && service.debts(today,true).getFirst().studentId()==1,"Delinquent-only filter");
            var clubs=service.clubOccupancy();var robotics=clubs.stream().filter(value -> value.clubId()==1).findFirst().orElseThrow();
            check(robotics.activeMembers()==2 && robotics.historicalStudents()==2 && robotics.availableSeats()==1 && robotics.occupancyRate().compareTo(new BigDecimal("66.67"))==0,"Club rejoining does not inflate historical students");
            check(clubs.stream().filter(value -> value.clubId()==2).findFirst().orElseThrow().activeMembers()==0,"Inactive empty clubs remain visible");
            var events=service.eventParticipation(from,to);
            check(events.size()==2 && events.getFirst().participants()==2 && events.getFirst().competitors()==1 && events.getFirst().awards()==1 && events.get(1).participants()==0,"Event totals and events without participants");
            var participation=service.studentParticipation();var studentParticipation=participation.stream().filter(value -> value.studentId()==1).findFirst().orElseThrow();
            check(participation.size()==3 && studentParticipation.activeClubs()==1 && studentParticipation.historicalClubs()==1 && studentParticipation.events()==1,"Joining club and event totals does not multiply records");
            check(participation.stream().filter(value -> value.studentId()==3).findFirst().orElseThrow().events()==0,"Students without activities remain visible");
            var inventory=service.inventory();var books=inventory.stream().filter(value -> value.categoryId()==1).findFirst().orElseThrow();
            check(inventory.size()==3 && books.total()==3 && books.available()==1 && books.loaned()==2 && inventory.stream().filter(value -> value.categoryId()==3).findFirst().orElseThrow().total()==0,"Inventory categories include empty groups");
            var overdue=service.overdueLoans(today);
            check(overdue.size()==1 && overdue.getFirst().borrowerType()==BorrowerType.STUDENT && overdue.getFirst().borrowerName().equals("Ana Perez") && overdue.getFirst().overdueDays()==2,"Overdue loan report resolves borrower and duration");
            check(service.overdueLoans(today.plusDays(1)).size()==2 && service.overdueLoans(today.plusDays(1)).stream().anyMatch(value -> value.borrowerType()==BorrowerType.TEACHER && value.borrowerName().equals("Maria Gil")),"Teacher and student overdue loans remain distinguishable");
            rejected(() -> service.overview(null));rejected(() -> service.income(to,from));rejected(() -> service.attendance(null,to));
            rejected(() -> service.studentAverages(0,AcademicPeriod.FIRST));rejected(() -> service.subjectAverages(2026,null));
            rejected(() -> service.studentsBelowAverage(2026,AcademicPeriod.FIRST,new BigDecimal("5.1")));
            rejected(() -> service.weeklyAbsenceAlerts(from,8));rejected(() -> service.debts(null,false));
            check(queries[0]>0,"All report queries were validated as prepared non-locking SELECT statements");
            check(service.overview(today).equals(overview),"Reports leave underlying school counts unchanged");
        }
        System.out.println("Passed "+checks+" report integration checks using temporary tables and read-only SQL enforcement.");
    }
}
