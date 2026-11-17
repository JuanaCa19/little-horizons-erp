package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;

public interface AttendanceDAO {
    int create(Attendance value, Connection connection);
    Optional<Attendance> findById(Integer id, Connection connection);
    Optional<Attendance> findByKey(Integer studentId, Integer subjectId, LocalDate date, Connection connection);
    List<Attendance> findByStudentAndDates(Integer studentId, LocalDate from, LocalDate to);
    List<Attendance> findBySubjectAndDate(Integer subjectId, LocalDate date);
    List<AbsenceAlert> findAbsenceAlerts(LocalDate from, LocalDate to, int threshold);
    void update(Attendance attendance, Connection connection);
}
