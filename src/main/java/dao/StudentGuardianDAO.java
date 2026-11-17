package dao;

import model.StudentGuardian;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface StudentGuardianDAO {

    void link(StudentGuardian relation);

    void link(StudentGuardian relation, Connection connection);

    List<StudentGuardian> findByStudentId(Integer studentId);

    List<StudentGuardian> findByGuardianId(Integer guardianId);

    List<StudentGuardian> findFinancialResponsibles(Integer studentId);

    Optional<StudentGuardian> findByIds(Integer studentId, Integer guardianId);

    void changePrimary(Integer studentId, Integer guardianId);

    void updateRelation(StudentGuardian relation);

    void unlink(Integer studentId, Integer guardianId);
}
