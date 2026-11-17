package service;

import dao.EnrollmentDAO;
import dao.GradeDAO;
import dao.GuardianDAO;
import dao.StudentDAO;
import dao.StudentGuardianDAO;
import dao.impl.EnrollmentDAOImpl;
import dao.impl.GradeDAOImpl;
import dao.impl.GuardianDAOImpl;
import dao.impl.StudentDAOImpl;
import dao.impl.StudentGuardianDAOImpl;
import model.Enrollment;
import model.Guardian;
import model.Student;
import model.StudentGuardian;
import model.enums.DocumentType;
import model.enums.EnrollmentStatus;
import model.enums.StudentStatus;
import util.ConnectionDB;
import util.exception.ValidationException;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static service.ServiceValidation.id;
import static service.ServiceValidation.relation;
import static service.ServiceValidation.require;
import static service.ServiceValidation.student;
import static service.ServiceValidation.text;

public class StudentService {

    private final StudentDAO studentDAO;
    private final GuardianDAO guardianDAO;
    private final StudentGuardianDAO relationDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GuardianService guardianService;
    private final EnrollmentService enrollmentService;
    private final TransactionManager transactions;

    public StudentService() {
        this(new StudentDAOImpl(), new GuardianDAOImpl(), new StudentGuardianDAOImpl(),
                new EnrollmentDAOImpl(), new GradeDAOImpl(), ConnectionDB::getConnection);
    }

    public StudentService(StudentDAO studentDAO, GuardianDAO guardianDAO, StudentGuardianDAO relationDAO,
                          EnrollmentDAO enrollmentDAO, GradeDAO gradeDAO, Supplier<Connection> connections) {
        this.studentDAO = Objects.requireNonNull(studentDAO);
        this.guardianDAO = Objects.requireNonNull(guardianDAO);
        this.relationDAO = Objects.requireNonNull(relationDAO);
        this.enrollmentDAO = Objects.requireNonNull(enrollmentDAO);
        this.guardianService = new GuardianService(guardianDAO);
        this.enrollmentService = new EnrollmentService(enrollmentDAO, studentDAO, gradeDAO, relationDAO);
        this.transactions = new TransactionManager(connections);
    }

    /**
     * Registers a student with a new guardian (null id) or an existing guardian (non-null id).
     * Generated identifiers are assigned to the supplied objects only after commit.
     */
    public int register(Student student, Guardian guardian, StudentGuardian relation, Enrollment enrollment) {
        student(student);
        require(student.getId() == null, "El nuevo estudiante no debe tener identificador.");
        require(student.getStatus() == StudentStatus.ACTIVE, "El nuevo estudiante debe estar activo.");
        require(!studentDAO.existsByDocument(student.getDocumentType(), student.getDocumentNumber()),
                "Ya existe un estudiante con ese documento.");
        require(guardian != null, "El acudiente es obligatorio.");

        boolean newGuardian = guardian.getId() == null;
        if (newGuardian) {
            guardianService.validateForCreation(guardian);
        } else {
            guardianService.getById(guardian.getId());
        }

        relation(relation);
        require(relation.getStudentId() == null, "El vinculo no debe pertenecer a otro estudiante.");
        require(relation.getGuardianId() == null || Objects.equals(relation.getGuardianId(), guardian.getId()),
                "El acudiente del vinculo no coincide con el seleccionado.");

        enrollmentService.validateNew(enrollment);
        require(enrollment.getStudentId() == null, "La matricula no debe pertenecer a otro estudiante.");
        require(!enrollment.getEnrollmentDate().isBefore(student.getAdmissionDate()),
                "La matricula no puede ser anterior al ingreso del estudiante.");

        int[] ids = transactions.execute(connection -> {
            int studentId = studentDAO.create(student, connection);
            int guardianId = newGuardian ? guardianDAO.create(guardian, connection) : guardian.getId();

            StudentGuardian newRelation = new StudentGuardian(
                    guardianId,
                    studentId,
                    relation.getRelationShip(),
                    true,
                    relation.isFinancialResponsible(),
                    relation.isEmergencyContact()
            );
            relationDAO.link(newRelation, connection);

            Enrollment newEnrollment = new Enrollment(
                    null,
                    studentId,
                    enrollment.getGradeId(),
                    enrollment.getAcademicYear(),
                    EnrollmentStatus.ACTIVE,
                    enrollment.getEnrollmentDate(),
                    null,
                    enrollment.getNotes()
            );
            int enrollmentId = enrollmentDAO.create(newEnrollment, connection);
            return new int[]{studentId, guardianId, enrollmentId};
        });

        student.setId(ids[0]);
        guardian.setId(ids[1]);
        relation.setStudentId(ids[0]);
        relation.setGuardianId(ids[1]);
        relation.setPrimary(true);
        enrollment.setStudentId(ids[0]);
        enrollment.setId(ids[2]);

        return ids[0];
    }

    public Student getById(Integer studentId) {
        id(studentId);
        return studentDAO.findById(studentId)
                .orElseThrow(() -> new ValidationException("Estudiante no encontrado."));
    }

    public Optional<Student> findByDocument(String documentNumber, DocumentType documentType) {
        require(documentType != null, "El tipo de documento es obligatorio.");
        return studentDAO.findByDocument(text(documentNumber, "El documento"), documentType);
    }

    public List<Student> findAll() {
        return studentDAO.findAll();
    }

    public List<Student> searchByName(String name) {
        return studentDAO.searchByName(text(name, "El nombre"));
    }

    public void update(Student student) {
        student(student);
        Student existing = getById(student.getId());
        require(student.getStatus() == existing.getStatus(), "Use la operacion de cambio de estado.");

        studentDAO.findByDocument(student.getDocumentNumber(), student.getDocumentType())
                .ifPresent(other -> require(Objects.equals(other.getId(), student.getId()),
                        "El documento pertenece a otro estudiante."));

        if (!student.getAdmissionDate().equals(existing.getAdmissionDate())) {
            require(enrollmentDAO.findByStudentId(student.getId()).stream()
                            .noneMatch(value -> value.getEnrollmentDate().isBefore(student.getAdmissionDate())),
                    "La fecha de ingreso no puede ser posterior a una matricula existente.");
        }
        studentDAO.update(student);
    }

    public void updateStatus(Integer studentId, StudentStatus status) {
        Student student = getById(studentId);
        require(status != null, "El estado del estudiante es obligatorio.");
        if (student.getStatus() == status) {
            return;
        }

        require(status != StudentStatus.WITHDRAWN && status != StudentStatus.GRADUATED
                        || enrollmentDAO.findByStudentId(studentId).stream()
                            .noneMatch(value -> value.getStatus() == EnrollmentStatus.ACTIVE),
                "Debe cerrar las matriculas activas antes de retirar o graduar al estudiante.");

        studentDAO.updateStatus(status, studentId);
    }
}
