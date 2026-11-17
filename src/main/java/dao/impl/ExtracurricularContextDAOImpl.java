package dao.impl;

import dao.ExtracurricularContextDAO;
import java.sql.Connection;
import java.time.LocalDate;
import model.Student;
import util.exception.ValidationException;

public class ExtracurricularContextDAOImpl implements ExtracurricularContextDAO {
    @Override public LocalDate requireStudent(Integer studentId,boolean active,Connection c) {
        Student student=JdbcSupport.query(c,"SELECT fecha_ingreso,estado FROM estudiantes WHERE id_estudiante=? FOR UPDATE",ps -> ps.setInt(1,studentId),rs -> {
            Student value=new Student();value.setAdmissionDate(rs.getObject("fecha_ingreso",LocalDate.class));
            value.setStatus(model.enums.StudentStatus.fromDbValue(rs.getString("estado")));return value;
        }).stream().findFirst().orElseThrow(() -> new ValidationException("Estudiante no encontrado."));
        if(active && student.getStatus()!=model.enums.StudentStatus.ACTIVE) throw new ValidationException("El estudiante debe estar activo.");
        return student.getAdmissionDate();
    }
    @Override public void requireActiveTeacher(Integer teacherId,Connection c) {
        if(JdbcSupport.query(c,"SELECT id_profesor FROM profesores WHERE id_profesor=? AND estado='Activo' FOR UPDATE",ps -> ps.setInt(1,teacherId),rs -> rs.getInt(1)).isEmpty())
            throw new ValidationException("El profesor no existe o no esta activo.");
    }
}
