package service;

import dao.TeacherDAO;
import dao.impl.TeacherDAOImpl;
import java.util.*;
import model.Teacher;
import model.enums.*;
import util.exception.ValidationException;
import static service.ServiceValidation.*;

public class TeacherService {
    private final TeacherDAO teacherDAO;
    public TeacherService() { this(new TeacherDAOImpl()); }
    public TeacherService(TeacherDAO teacherDAO) { this.teacherDAO = Objects.requireNonNull(teacherDAO); }

    private void validate(Teacher teacher) {
        require(teacher != null, "El profesor es obligatorio.");
        require(teacher.getDocumentType() != null && teacher.getDocumentType() != DocumentType.CIVIL_REGISTRY
                && teacher.getDocumentType() != DocumentType.IDENTITY_CARD, "Tipo de documento no permitido para profesores.");
        teacher.setDocumentNumber(text(teacher.getDocumentNumber(), "El documento"));
        require(teacher.getDocumentNumber().length() <= 20, "El documento admite hasta 20 caracteres.");
        text(teacher.getFirstName(), "El primer nombre");
        text(teacher.getFirstLastName(), "El primer apellido");
        teacher.setEmail(text(teacher.getEmail(), "El correo electronico"));
        email(teacher.getEmail());
        for (String part : Arrays.asList(teacher.getFirstName(), teacher.getMiddleName(), teacher.getFirstLastName(),
                teacher.getSecondLastName(), teacher.getEmail(), teacher.getSpecialization()))
            require(part == null || part.length() <= 100, "Nombres, correo y especializacion admiten hasta 100 caracteres.");
        require(teacher.getPhone() == null || teacher.getPhone().length() <= 20, "El telefono admite hasta 20 caracteres.");
        require(teacher.getHiringDate() != null, "La fecha de contratacion es obligatoria.");
        require(teacher.getStatus() != null, "El estado del profesor es obligatorio.");
    }
    private void checkUnique(Teacher teacher) {
        teacherDAO.findByDocument(teacher.getDocumentNumber(), teacher.getDocumentType()).ifPresent(existing ->
                require(teacher.getId() != null && Objects.equals(existing.getId(), teacher.getId()),
                        "Ya existe un profesor con ese documento."));
        teacherDAO.findByEmail(teacher.getEmail()).ifPresent(existing ->
                require(teacher.getId() != null && Objects.equals(existing.getId(), teacher.getId()),
                        "El correo pertenece a otro profesor."));
    }
    public int create(Teacher teacher) {
        validate(teacher);
        require(teacher.getId() == null, "El nuevo profesor no debe tener identificador.");
        checkUnique(teacher);
        int id = teacherDAO.create(teacher);
        teacher.setId(id);
        return id;
    }
    public Teacher getById(Integer teacherId) {
        id(teacherId);
        return teacherDAO.findById(teacherId).orElseThrow(() -> new ValidationException("Profesor no encontrado."));
    }
    public List<Teacher> findAll() { return teacherDAO.findAll(); }
    public List<Teacher> searchByName(String name) { return teacherDAO.searchByName(text(name, "El nombre")); }
    public Optional<Teacher> findByDocument(String number, DocumentType type) {
        require(type != null, "El tipo de documento es obligatorio.");
        return teacherDAO.findByDocument(text(number, "El documento"), type);
    }
    public void update(Teacher teacher) {
        validate(teacher);
        Teacher existing = getById(teacher.getId());
        require(existing.getStatus() == teacher.getStatus(), "Use la operacion de cambio de estado.");
        checkUnique(teacher);
        teacherDAO.update(teacher);
    }
    public void updateStatus(Integer teacherId, TeacherStatus status) {
        Teacher existing = getById(teacherId);
        require(status != null, "El estado es obligatorio.");
        if (existing.getStatus() != status) teacherDAO.updateStatus(teacherId, status);
    }
}
