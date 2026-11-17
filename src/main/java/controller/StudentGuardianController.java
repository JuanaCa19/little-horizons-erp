package controller;

import java.util.List;
import java.util.Objects;
import model.StudentGuardian;
import service.StudentGuardianService;

/**
 * Exposes studentGuardian operations to views without console or persistence dependencies.
 * Validation and data access exceptions propagate to the calling view.
 */
public class StudentGuardianController {
    private final StudentGuardianService studentGuardianService;

    public StudentGuardianController() {
        this(new StudentGuardianService());
    }

    public StudentGuardianController(StudentGuardianService studentGuardianService) {
        this.studentGuardianService = Objects.requireNonNull(studentGuardianService);
    }

    public void link(StudentGuardian relation) {
        studentGuardianService.link(relation);
    }

    public StudentGuardian getByIds(Integer studentId, Integer guardianId) {
        return studentGuardianService.getByIds(studentId, guardianId);
    }

    public List<StudentGuardian> findByStudentId(Integer studentId) {
        return studentGuardianService.findByStudentId(studentId);
    }

    public List<StudentGuardian> findByGuardianId(Integer guardianId) {
        return studentGuardianService.findByGuardianId(guardianId);
    }

    public List<StudentGuardian> findFinancialResponsibles(Integer studentId) {
        return studentGuardianService.findFinancialResponsibles(studentId);
    }

    public void changePrimary(Integer studentId, Integer guardianId) {
        studentGuardianService.changePrimary(studentId, guardianId);
    }

    public void updateRelation(StudentGuardian relation) {
        studentGuardianService.updateRelation(relation);
    }

    public void unlink(Integer studentId, Integer guardianId) {
        studentGuardianService.unlink(studentId, guardianId);
    }
}
