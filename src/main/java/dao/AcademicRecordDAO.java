package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import model.*;
import model.enums.*;

public interface AcademicRecordDAO {
    int create(AcademicRecord value, Connection connection);
    Optional<AcademicRecord> findById(Integer id, Connection connection);
    Optional<AcademicRecord> findByKey(Integer studentId, Integer subjectId, int year, AcademicPeriod period, Connection connection);
    List<AcademicRecord> findByStudentId(Integer studentId);
    List<AcademicRecord> findBySubjectAndPeriod(Integer subjectId, int year, AcademicPeriod period);
    void update(AcademicRecord record, Connection connection);
}
