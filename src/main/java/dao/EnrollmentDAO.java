package dao;

import model.Enrollment;
import model.enums.EnrollmentStatus;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnrollmentDAO {

    int create(Enrollment enrollment);

    int create(Enrollment enrollment, Connection connection);

    Optional<Enrollment> findById(Integer id);

    Optional<Enrollment> findByStudentAndYear(Integer studentId, Integer academicYear);

    List<Enrollment> findByStudentId(Integer studentId);

    List<Enrollment> findByYearAndGrade(Integer academicYear, Integer gradeId);

    void updateStatus(EnrollmentStatus status, LocalDate withdrawalDate, Integer id);
}
