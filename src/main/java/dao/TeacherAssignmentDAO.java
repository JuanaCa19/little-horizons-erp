package dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import model.*;
import model.enums.*;

public interface TeacherAssignmentDAO {
    int create(TeacherAssignment assignment, Connection connection);
    Optional<TeacherAssignment> findById(Integer id);
    boolean exists(Integer teacherId, Integer subjectId, int year, String section, Connection connection);
    List<TeacherAssignment> findByTeacherAndYear(Integer teacherId, int year);
}
