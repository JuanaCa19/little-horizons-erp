package dao;

import java.sql.Connection;
import java.time.LocalDate;

public interface ExtracurricularContextDAO {
    LocalDate requireStudent(Integer studentId,boolean active,Connection connection);
    void requireActiveTeacher(Integer teacherId,Connection connection);
}
