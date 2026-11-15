package dao;


import model.Student;
import model.StudentGuardian;
import java.sql.Connection;
import java.util.List;


public interface StudentGuardianDAO {
    void link(StudentGuardian studentGuardian);

    void link(StudentGuardian studentGuardian, Connection connection);

    List<StudentGuardian> findByStudentId(Integer studentId);

    List<Student> findByGuardianId(Integer guardianId);

    List<StudentGuardian> findFinancialResponsible(Integer studentId);

    void changePrimary(Integer studentId, Integer guardianId);

    void updateRelation(StudentGuardian relation);

    void unlink(Integer studentId, Integer guardianId);
}
