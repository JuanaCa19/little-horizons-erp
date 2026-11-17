package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;

public interface SubjectEnrollmentDAO {
    int create(SubjectEnrollment value, Connection connection);
    Optional<SubjectEnrollment> findById(Integer id, Connection connection);
    Optional<SubjectEnrollment> findByKey(Integer studentId, Integer subjectId, int year, AcademicPeriod period, Connection connection);
    List<SubjectEnrollment> findByStudentAndYear(Integer studentId, int year);
    List<SubjectEnrollment> findBySubjectAndPeriod(Integer subjectId, int year, AcademicPeriod period);
    void updateStatus(Integer id, EnrollmentStatus status, Connection connection);
}
