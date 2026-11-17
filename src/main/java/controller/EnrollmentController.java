package controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import model.Enrollment;
import model.enums.EnrollmentStatus;
import service.EnrollmentService;

/**
 * Exposes enrollment operations to views without console or persistence dependencies.
 * Validation and data access exceptions propagate to the calling view.
 */
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController() {
        this(new EnrollmentService());
    }

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = Objects.requireNonNull(enrollmentService);
    }

    public int create(Enrollment enrollment) {
        return enrollmentService.create(enrollment);
    }

    public Enrollment getById(Integer enrollmentId) {
        return enrollmentService.getById(enrollmentId);
    }

    public Optional<Enrollment> findByStudentAndYear(Integer studentId, int academicYear) {
        return enrollmentService.findByStudentAndYear(studentId, academicYear);
    }

    public List<Enrollment> findByStudentId(Integer studentId) {
        return enrollmentService.findByStudentId(studentId);
    }

    public List<Enrollment> findByYearAndGrade(int academicYear, Integer gradeId) {
        return enrollmentService.findByYearAndGrade(academicYear, gradeId);
    }

    public void updateStatus(Integer enrollmentId, EnrollmentStatus status, LocalDate withdrawalDate) {
        enrollmentService.updateStatus(enrollmentId, status, withdrawalDate);
    }
}
