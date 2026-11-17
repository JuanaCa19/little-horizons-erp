package controller;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import model.Student;
import model.Guardian;
import model.StudentGuardian;
import model.Enrollment;
import model.enums.DocumentType;
import model.enums.StudentStatus;
import service.StudentService;

/**
 * Exposes student operations to views without console or persistence dependencies.
 * Validation and data access exceptions propagate to the calling view.
 */
public class StudentController {
    private final StudentService studentService;

    public StudentController() {
        this(new StudentService());
    }

    public StudentController(StudentService studentService) {
        this.studentService = Objects.requireNonNull(studentService);
    }

    public int register(Student student, Guardian guardian, StudentGuardian relation, Enrollment enrollment) {
        return studentService.register(student, guardian, relation, enrollment);
    }

    public Student getById(Integer studentId) {
        return studentService.getById(studentId);
    }

    public Optional<Student> findByDocument(String documentNumber, DocumentType documentType) {
        return studentService.findByDocument(documentNumber, documentType);
    }

    public List<Student> findAll() {
        return studentService.findAll();
    }

    public List<Student> searchByName(String name) {
        return studentService.searchByName(name);
    }

    public void update(Student student) {
        studentService.update(student);
    }

    public void updateStatus(Integer studentId, StudentStatus status) {
        studentService.updateStatus(studentId, status);
    }
}
