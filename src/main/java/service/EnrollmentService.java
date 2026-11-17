package service;

import dao.EnrollmentDAO;
import dao.GradeDAO;
import dao.StudentDAO;
import dao.StudentGuardianDAO;
import dao.impl.EnrollmentDAOImpl;
import dao.impl.GradeDAOImpl;
import dao.impl.StudentDAOImpl;
import dao.impl.StudentGuardianDAOImpl;
import model.Enrollment;
import model.Student;
import model.enums.EnrollmentStatus;
import model.enums.StudentStatus;
import util.exception.ValidationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static service.ServiceValidation.enrollment;
import static service.ServiceValidation.id;
import static service.ServiceValidation.require;
import static service.ServiceValidation.year;

public class EnrollmentService {

    private final EnrollmentDAO enrollmentDAO;
    private final StudentDAO studentDAO;
    private final GradeService gradeService;
    private final StudentGuardianDAO relationDAO;

    public EnrollmentService() {
        this(new EnrollmentDAOImpl(), new StudentDAOImpl(), new GradeDAOImpl(), new StudentGuardianDAOImpl());
    }

    public EnrollmentService(EnrollmentDAO enrollmentDAO, StudentDAO studentDAO,
                           GradeDAO gradeDAO, StudentGuardianDAO relationDAO) {
        this.enrollmentDAO = Objects.requireNonNull(enrollmentDAO);
        this.studentDAO = Objects.requireNonNull(studentDAO);
        this.gradeService = new GradeService(gradeDAO);
        this.relationDAO = Objects.requireNonNull(relationDAO);
    }

    public int create(Enrollment enrollment) {
        validateNew(enrollment);
        id(enrollment.getStudentId());

        Student student = studentDAO.findById(enrollment.getStudentId())
                .orElseThrow(() -> new ValidationException("Estudiante no encontrado."));

        require(student.getStatus() == StudentStatus.ACTIVE, "Solo se pueden matricular estudiantes activos.");
        require(!enrollment.getEnrollmentDate().isBefore(student.getAdmissionDate()),
                "La matricula no puede ser anterior al ingreso del estudiante.");
        require(!relationDAO.findByStudentId(student.getId()).isEmpty(),
                "El estudiante debe tener al menos un acudiente.");
        require(enrollmentDAO.findByStudentAndYear(student.getId(), enrollment.getAcademicYear()).isEmpty(),
                "El estudiante ya tiene una matricula para ese anio.");

        int id = enrollmentDAO.create(enrollment);
        enrollment.setId(id);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        return id;
    }

    void validateNew(Enrollment enrollment) {
        enrollment(enrollment);
        require(enrollment.getId() == null, "La nueva matricula no debe tener identificador.");
        require(enrollment.getStatus() == null || enrollment.getStatus() == EnrollmentStatus.ACTIVE,
                "Una nueva matricula debe estar activa.");
        require(enrollment.getWithdrawalDate() == null, "Una nueva matricula no puede tener fecha de retiro.");

        gradeService.getById(enrollment.getGradeId());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
    }

    public Enrollment getById(Integer enrollmentId) {
        id(enrollmentId);
        return enrollmentDAO.findById(enrollmentId)
                .orElseThrow(() -> new ValidationException("Matricula no encontrada."));
    }

    public Optional<Enrollment> findByStudentAndYear(Integer studentId, int academicYear) {
        id(studentId);
        year(academicYear);
        return enrollmentDAO.findByStudentAndYear(studentId, academicYear);
    }

    public List<Enrollment> findByStudentId(Integer studentId) {
        id(studentId);
        return enrollmentDAO.findByStudentId(studentId);
    }

    public List<Enrollment> findByYearAndGrade(int academicYear, Integer gradeId) {
        year(academicYear);
        gradeService.getById(gradeId);
        return enrollmentDAO.findByYearAndGrade(academicYear, gradeId);
    }

    public void updateStatus(Integer enrollmentId, EnrollmentStatus status, LocalDate withdrawalDate) {
        Enrollment enrollment = getById(enrollmentId);
        require(status != null, "El estado de matricula es obligatorio.");
        require(status == enrollment.getStatus() || enrollment.getStatus() == EnrollmentStatus.ACTIVE,
                "Una matricula cerrada no puede cambiar de estado.");

        if (status == EnrollmentStatus.WITHDRAWN) {
            require(withdrawalDate != null
                            && !withdrawalDate.isBefore(enrollment.getEnrollmentDate())
                            && !withdrawalDate.isAfter(LocalDate.now()),
                    "La fecha de retiro debe estar entre la matricula y hoy.");
        } else {
            require(withdrawalDate == null, "Solo una matricula retirada puede tener fecha de retiro.");
        }

        if (status == enrollment.getStatus() && Objects.equals(withdrawalDate, enrollment.getWithdrawalDate())) {
            return;
        }

        enrollmentDAO.updateStatus(status, withdrawalDate, enrollmentId);
    }
}
