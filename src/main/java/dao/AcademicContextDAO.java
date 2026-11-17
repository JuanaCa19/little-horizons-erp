package dao;

import java.sql.Connection;
import java.util.Optional;
import model.Enrollment;

public interface AcademicContextDAO {
    void lockStudent(Integer studentId, Connection connection);
    boolean isStudentActive(Integer studentId, Connection connection);
    Optional<Enrollment> findEnrollment(Integer studentId, int year, Connection connection);
    boolean isTeacherAssigned(Integer teacherId, Integer subjectId, int year, String section, Connection connection);
}
