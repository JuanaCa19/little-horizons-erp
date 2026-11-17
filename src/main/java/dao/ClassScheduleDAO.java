package dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import model.*;
import model.enums.*;

public interface ClassScheduleDAO {
    int create(ClassSchedule schedule, Connection connection);
    Optional<ClassSchedule> findById(Integer id);
    List<ClassSchedule> findByAssignmentId(Integer assignmentId);
    List<ClassSchedule> findByTeacherAndYear(Integer teacherId, int year);
    boolean hasConflict(TeacherAssignment assignment, Integer gradeId, ClassSchedule schedule, Connection connection);
    void lockGrade(Integer gradeId, Connection connection);
    void delete(Integer id);
}
