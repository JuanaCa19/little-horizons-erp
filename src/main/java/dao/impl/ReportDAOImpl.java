package dao.impl;

import dao.ReportDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import model.enums.*;
import model.report.*;

/** Aggregates stored data with SELECT statements only. */
public class ReportDAOImpl implements ReportDAO {
    private static final String STUDENT_NAME="CONCAT_WS(' ',e.primer_nombre,e.segundo_nombre,e.primer_apellido,e.segundo_apellido)";
    private String allLoans() {
        return "(SELECT id_prestamo,id_recurso,id_estudiante borrower_id,'STUDENT' borrower_type,fecha_devolucion_esperada,estado_prestamo FROM prestamos_estudiante "
                + "UNION ALL SELECT id_prestamo,id_recurso,id_profesor borrower_id,'TEACHER' borrower_type,fecha_devolucion_esperada,estado_prestamo FROM prestamos_profesor) l";
    }
    @Override public SchoolOverview overview(LocalDate date,Connection c) {
        long[] students=JdbcSupport.query(c,"SELECT COUNT(*) total,COALESCE(SUM(estado='Activo'),0) active FROM estudiantes",ps -> {},rs -> new long[]{rs.getLong("total"),rs.getLong("active")}).getFirst();
        long teachers=JdbcSupport.query(c,"SELECT COUNT(*) FROM profesores WHERE estado='Activo'",ps -> {},rs -> rs.getLong(1)).getFirst();
        long clubs=JdbcSupport.query(c,"SELECT COUNT(*) FROM clubes WHERE esta_activo=TRUE",ps -> {},rs -> rs.getLong(1)).getFirst();
        long[] resources=JdbcSupport.query(c,"SELECT COUNT(*) total,COALESCE(SUM(estado_recurso='Disponible'),0) available FROM recursos",ps -> {},rs -> new long[]{rs.getLong("total"),rs.getLong("available")}).getFirst();
        long[] loans=JdbcSupport.query(c,"SELECT COUNT(*) total,COALESCE(SUM(fecha_devolucion_esperada<?),0) overdue FROM "+allLoans()+" WHERE estado_prestamo IN ('Activo','Vencido')",
                ps -> ps.setObject(1,date,Types.DATE),rs -> new long[]{rs.getLong("total"),rs.getLong("overdue")}).getFirst();
        return new SchoolOverview(students[0],students[1],teachers,clubs,resources[0],resources[1],loans[0],loans[1]);
    }
    @Override public List<GradeEnrollmentCount> enrollmentByGrade(int year,Connection c) {
        String sql="SELECT g.id_grado,g.nombre_grado,COUNT(m.id_matricula) total,COALESCE(SUM(m.estado_matricula='Activa'),0) active,"
                + "COALESCE(SUM(m.estado_matricula='Completada'),0) completed,COALESCE(SUM(m.estado_matricula='Retirada'),0) withdrawn "
                + "FROM grados g LEFT JOIN matriculas m ON m.id_grado=g.id_grado AND m.anio_academico=? GROUP BY g.id_grado,g.nombre_grado,g.nivel_numerico ORDER BY g.nivel_numerico";
        return JdbcSupport.query(c,sql,ps -> ps.setInt(1,year),rs -> new GradeEnrollmentCount(rs.getInt("id_grado"),rs.getString("nombre_grado"),rs.getLong("total"),rs.getLong("active"),rs.getLong("completed"),rs.getLong("withdrawn")));
    }
    @Override public List<StudentAverageReport> studentAverages(int year,AcademicPeriod period,Connection c) {
        String sql="SELECT e.id_estudiante,"+STUDENT_NAME+" student_name,g.nombre_grado,ROUND(AVG(r.valor_nota),2) average,COUNT(*) subjects "
                + "FROM registros_academicos r JOIN estudiantes e ON e.id_estudiante=r.id_estudiante LEFT JOIN matriculas m ON m.id_estudiante=e.id_estudiante AND m.anio_academico=r.anio_academico "
                + "LEFT JOIN grados g ON g.id_grado=m.id_grado WHERE r.anio_academico=? AND r.periodo_academico=? "
                + "GROUP BY e.id_estudiante,e.primer_nombre,e.segundo_nombre,e.primer_apellido,e.segundo_apellido,g.nombre_grado ORDER BY average DESC,student_name,e.id_estudiante";
        return JdbcSupport.query(c,sql,ps -> { ps.setInt(1,year);ps.setString(2,period.getDbValue()); },rs -> new StudentAverageReport(rs.getInt("id_estudiante"),rs.getString("student_name"),rs.getString("nombre_grado"),rs.getBigDecimal("average"),rs.getLong("subjects")));
    }
    @Override public List<SubjectAverageReport> subjectAverages(int year,AcademicPeriod period,Connection c) {
        String sql="SELECT s.id_materia,s.nombre_materia,g.nombre_grado,i.seccion,ROUND(AVG(r.valor_nota),2) average,COUNT(*) students "
                + "FROM registros_academicos r JOIN materias s ON s.id_materia=r.id_materia JOIN grados g ON g.id_grado=s.id_grado "
                + "LEFT JOIN inscripciones_materia i ON i.id_estudiante=r.id_estudiante AND i.id_materia=r.id_materia AND i.anio_academico=r.anio_academico AND i.periodo_academico=? "
                + "WHERE r.anio_academico=? AND r.periodo_academico=? GROUP BY s.id_materia,s.nombre_materia,g.id_grado,g.nombre_grado,g.nivel_numerico,i.seccion ORDER BY g.nivel_numerico,s.nombre_materia,i.seccion";
        return JdbcSupport.query(c,sql,ps -> { ps.setString(1,period==AcademicPeriod.FINAL ? "IV" : period.getDbValue());ps.setInt(2,year);ps.setString(3,period.getDbValue()); },
                rs -> new SubjectAverageReport(rs.getInt("id_materia"),rs.getString("nombre_materia"),rs.getString("nombre_grado"),rs.getString("seccion"),rs.getBigDecimal("average"),rs.getLong("students")));
    }
    @Override public List<TeacherWorkloadReport> teacherWorkload(int year,Connection c) {
        String sql="SELECT p.id_profesor,CONCAT_WS(' ',p.primer_nombre,p.segundo_nombre,p.primer_apellido,p.segundo_apellido) teacher_name,p.estado,COUNT(DISTINCT a.id_asignacion) assignments,"
                + "COALESCE(SUM(TIME_TO_SEC(TIMEDIFF(h.hora_fin,h.hora_inicio))/60),0) minutes FROM profesores p "
                + "LEFT JOIN asignaciones_profesor a ON a.id_profesor=p.id_profesor AND a.anio_academico=? LEFT JOIN horarios_asignacion h ON h.id_asignacion=a.id_asignacion "
                + "GROUP BY p.id_profesor,p.primer_nombre,p.segundo_nombre,p.primer_apellido,p.segundo_apellido,p.estado ORDER BY minutes DESC,teacher_name,p.id_profesor";
        return JdbcSupport.query(c,sql,ps -> ps.setInt(1,year),rs -> new TeacherWorkloadReport(rs.getInt("id_profesor"),rs.getString("teacher_name"),rs.getString("estado"),rs.getLong("assignments"),rs.getLong("minutes")));
    }
    @Override public List<AttendanceStatistics> attendance(LocalDate from,LocalDate to,Connection c) {
        String sql="SELECT e.id_estudiante,"+STUDENT_NAME+" student_name,SUM(a.estado_asistencia='Presente') present,SUM(a.estado_asistencia='Ausente') absent,SUM(a.estado_asistencia='Tardanza') late,"
                + "SUM(a.estado_asistencia NOT IN ('Presente','Ausente','Tardanza')) excused FROM asistencias a JOIN estudiantes e ON e.id_estudiante=a.id_estudiante WHERE a.fecha_asistencia BETWEEN ? AND ? "
                + "GROUP BY e.id_estudiante,e.primer_nombre,e.segundo_nombre,e.primer_apellido,e.segundo_apellido ORDER BY absent DESC,student_name,e.id_estudiante";
        return JdbcSupport.query(c,sql,ps -> { ps.setObject(1,from,Types.DATE);ps.setObject(2,to,Types.DATE); },rs -> new AttendanceStatistics(rs.getInt("id_estudiante"),rs.getString("student_name"),rs.getLong("present"),rs.getLong("absent"),rs.getLong("late"),rs.getLong("excused")));
    }
    @Override public List<IncomeReport> income(LocalDate from,LocalDate to,Connection c) {
        String sql="SELECT p.id_concepto,c.nombre_concepto,COUNT(*) payments,SUM(p.monto_pagado) total FROM pagos p JOIN conceptos_pago c ON c.id_concepto=p.id_concepto "
                + "WHERE p.fecha_pago BETWEEN ? AND ? GROUP BY p.id_concepto,c.nombre_concepto ORDER BY total DESC,p.id_concepto";
        return JdbcSupport.query(c,sql,ps -> { ps.setObject(1,from,Types.DATE);ps.setObject(2,to,Types.DATE); },rs -> new IncomeReport(rs.getInt("id_concepto"),rs.getString("nombre_concepto"),rs.getLong("payments"),rs.getBigDecimal("total")));
    }
    @Override public List<DebtReport> debts(LocalDate cutoff,Connection c) {
        String sql="SELECT e.id_estudiante,"+STUDENT_NAME+" student_name,COUNT(*) pending,SUM(c.monto_cargo) pending_base,SUM(c.fecha_vencimiento<?) overdue,"
                + "SUM(CASE WHEN c.fecha_vencimiento<? THEN c.monto_cargo ELSE 0 END) overdue_base,MIN(c.fecha_vencimiento) earliest FROM cargos_estudiante c "
                + "JOIN estudiantes e ON e.id_estudiante=c.id_estudiante WHERE c.estado='Pendiente' AND c.monto_cargo>0 GROUP BY e.id_estudiante,e.primer_nombre,e.segundo_nombre,e.primer_apellido,e.segundo_apellido ORDER BY overdue_base DESC,pending_base DESC,e.id_estudiante";
        return JdbcSupport.query(c,sql,ps -> { ps.setObject(1,cutoff,Types.DATE);ps.setObject(2,cutoff,Types.DATE); },rs -> new DebtReport(rs.getInt("id_estudiante"),rs.getString("student_name"),rs.getLong("pending"),rs.getBigDecimal("pending_base"),rs.getLong("overdue"),rs.getBigDecimal("overdue_base"),rs.getObject("earliest",LocalDate.class)));
    }
    @Override public List<ClubOccupancyReport> clubOccupancy(Connection c) {
        String sql="SELECT c.id_club,c.nombre_club,c.esta_activo,c.capacidad_maxima,COALESCE(SUM(m.estado_membresia='Activa'),0) active_members,COUNT(DISTINCT m.id_estudiante) historical FROM clubes c "
                + "LEFT JOIN membresias_club m ON m.id_club=c.id_club GROUP BY c.id_club,c.nombre_club,c.esta_activo,c.capacidad_maxima ORDER BY c.nombre_club,c.id_club";
        return JdbcSupport.query(c,sql,ps -> {},rs -> new ClubOccupancyReport(rs.getInt("id_club"),rs.getString("nombre_club"),rs.getBoolean("esta_activo"),rs.getInt("capacidad_maxima"),rs.getLong("active_members"),rs.getLong("historical")));
    }
    @Override public List<EventParticipationReport> eventParticipation(LocalDate from,LocalDate to,Connection c) {
        String sql="SELECT e.id_evento,e.nombre_evento,e.fecha_evento,e.tipo_evento,e.esta_activo,COUNT(p.id_participacion) participants,COALESCE(SUM(p.rol_participacion='Competidor'),0) competitors,"
                + "COALESCE(SUM(p.posicion_obtenida IN ('1er Lugar','2do Lugar','3er Lugar')),0) awards FROM eventos e LEFT JOIN participantes_evento p ON p.id_evento=e.id_evento "
                + "WHERE e.fecha_evento BETWEEN ? AND ? GROUP BY e.id_evento,e.nombre_evento,e.fecha_evento,e.tipo_evento,e.esta_activo ORDER BY e.fecha_evento,e.id_evento";
        return JdbcSupport.query(c,sql,ps -> { ps.setObject(1,from,Types.DATE);ps.setObject(2,to,Types.DATE); },rs -> new EventParticipationReport(rs.getInt("id_evento"),rs.getString("nombre_evento"),rs.getObject("fecha_evento",LocalDate.class),rs.getString("tipo_evento"),rs.getBoolean("esta_activo"),rs.getLong("participants"),rs.getLong("competitors"),rs.getLong("awards")));
    }
    @Override public List<StudentParticipationReport> studentParticipation(Connection c) {
        String sql="SELECT e.id_estudiante,"+STUDENT_NAME+" student_name,COALESCE(c.active_clubs,0) active_clubs,COALESCE(c.historical_clubs,0) historical_clubs,COALESCE(v.events,0) events FROM estudiantes e "
                + "LEFT JOIN (SELECT id_estudiante,SUM(estado_membresia='Activa') active_clubs,COUNT(DISTINCT id_club) historical_clubs FROM membresias_club GROUP BY id_estudiante) c ON c.id_estudiante=e.id_estudiante "
                + "LEFT JOIN (SELECT id_estudiante,COUNT(*) events FROM participantes_evento GROUP BY id_estudiante) v ON v.id_estudiante=e.id_estudiante ORDER BY events DESC,active_clubs DESC,student_name,e.id_estudiante";
        return JdbcSupport.query(c,sql,ps -> {},rs -> new StudentParticipationReport(rs.getInt("id_estudiante"),rs.getString("student_name"),rs.getLong("active_clubs"),rs.getLong("historical_clubs"),rs.getLong("events")));
    }
    @Override public List<ResourceInventoryReport> inventory(Connection c) {
        String sql="SELECT c.id_categoria,c.nombre_categoria,COUNT(r.id_recurso) total,COALESCE(SUM(r.estado_recurso='Disponible'),0) available,COALESCE(SUM(r.estado_recurso='Prestado'),0) loaned,"
                + "COALESCE(SUM(r.estado_recurso='En Mantenimiento'),0) maintenance,COALESCE(SUM(r.estado_recurso='Dado de Baja'),0) retired FROM categorias_recurso c "
                + "LEFT JOIN recursos r ON r.id_categoria=c.id_categoria GROUP BY c.id_categoria,c.nombre_categoria ORDER BY c.nombre_categoria,c.id_categoria";
        return JdbcSupport.query(c,sql,ps -> {},rs -> new ResourceInventoryReport(rs.getInt("id_categoria"),rs.getString("nombre_categoria"),rs.getLong("total"),rs.getLong("available"),rs.getLong("loaned"),rs.getLong("maintenance"),rs.getLong("retired")));
    }
    @Override public List<OverdueLoanReport> overdueLoans(LocalDate date,Connection c) {
        String sql="SELECT l.id_prestamo,l.borrower_type,l.borrower_id,l.id_recurso,r.nombre_recurso,l.fecha_devolucion_esperada,DATEDIFF(?,l.fecha_devolucion_esperada) overdue_days,"
                + "CASE WHEN l.borrower_type='STUDENT' THEN "+STUDENT_NAME+" ELSE CONCAT_WS(' ',p.primer_nombre,p.segundo_nombre,p.primer_apellido,p.segundo_apellido) END borrower_name FROM "+allLoans()
                + " JOIN recursos r ON r.id_recurso=l.id_recurso LEFT JOIN estudiantes e ON l.borrower_type='STUDENT' AND e.id_estudiante=l.borrower_id "
                + "LEFT JOIN profesores p ON l.borrower_type='TEACHER' AND p.id_profesor=l.borrower_id WHERE l.estado_prestamo IN ('Activo','Vencido') AND l.fecha_devolucion_esperada<? ORDER BY l.fecha_devolucion_esperada,l.borrower_type,l.id_prestamo";
        return JdbcSupport.query(c,sql,ps -> { ps.setObject(1,date,Types.DATE);ps.setObject(2,date,Types.DATE); },rs -> new OverdueLoanReport(rs.getInt("id_prestamo"),BorrowerType.valueOf(rs.getString("borrower_type")),rs.getInt("borrower_id"),rs.getString("borrower_name"),rs.getInt("id_recurso"),rs.getString("nombre_recurso"),rs.getObject("fecha_devolucion_esperada",LocalDate.class),rs.getLong("overdue_days")));
    }

    @Override public List<model.AbsenceAlert> absenceAlerts(LocalDate from,LocalDate to,int threshold,Connection c) {
        String sql="SELECT e.id_estudiante,"+STUDENT_NAME+" student_name,COUNT(DISTINCT a.fecha_asistencia) days FROM asistencias a JOIN estudiantes e ON e.id_estudiante=a.id_estudiante "
                + "WHERE a.estado_asistencia='Ausente' AND a.fecha_asistencia BETWEEN ? AND ? GROUP BY e.id_estudiante,e.primer_nombre,e.segundo_nombre,e.primer_apellido,e.segundo_apellido HAVING COUNT(DISTINCT a.fecha_asistencia)>? ORDER BY days DESC,e.id_estudiante";
        return JdbcSupport.query(c,sql,ps -> { ps.setObject(1,from,Types.DATE);ps.setObject(2,to,Types.DATE);ps.setInt(3,threshold); },rs -> new model.AbsenceAlert(rs.getInt("id_estudiante"),rs.getString("student_name"),rs.getInt("days")));
    }
}
