package dao.impl;

import dao.AcademicContextDAO;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import model.Enrollment;
import model.enums.EnrollmentStatus;
import util.exception.ValidationException;

public class AcademicContextDAOImpl implements AcademicContextDAO {
    @Override public void lockStudent(Integer studentId, Connection c) {
        if (JdbcSupport.query(c,"SELECT id_estudiante FROM estudiantes WHERE id_estudiante=? FOR UPDATE",ps -> ps.setInt(1,studentId),rs -> rs.getInt(1)).isEmpty())
            throw new ValidationException("Estudiante no encontrado.");
    }
    @Override public boolean isStudentActive(Integer studentId, Connection c) {
        return !JdbcSupport.query(c,"SELECT id_estudiante FROM estudiantes WHERE id_estudiante=? AND estado='Activo'",ps -> ps.setInt(1,studentId),rs -> rs.getInt(1)).isEmpty();
    }
    @Override public Optional<Enrollment> findEnrollment(Integer studentId,int year,Connection c) {
        return JdbcSupport.query(c,"SELECT * FROM matriculas WHERE id_estudiante=? AND anio_academico=?",ps -> { ps.setInt(1,studentId); ps.setInt(2,year); },rs ->
                new Enrollment(rs.getInt("id_matricula"),rs.getInt("id_estudiante"),rs.getInt("id_grado"),rs.getInt("anio_academico"),
                        EnrollmentStatus.fromDbValue(rs.getString("estado_matricula")),rs.getObject("fecha_matricula",LocalDate.class),rs.getObject("fecha_retiro",LocalDate.class),rs.getString("observaciones"))).stream().findFirst();
    }
    @Override public boolean isTeacherAssigned(Integer teacherId,Integer subjectId,int year,String section,Connection c) {
        return !JdbcSupport.query(c,"SELECT a.id_asignacion FROM asignaciones_profesor a JOIN profesores p ON p.id_profesor=a.id_profesor "
                + "WHERE a.id_profesor=? AND a.id_materia=? AND a.anio_academico=? AND a.seccion=? AND p.estado='Activo'",ps -> {
                    ps.setInt(1,teacherId);ps.setInt(2,subjectId);ps.setInt(3,year);ps.setString(4,section);
                },rs -> rs.getInt(1)).isEmpty();
    }
}
