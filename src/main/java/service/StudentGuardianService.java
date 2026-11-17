package service;

import dao.GuardianDAO;
import dao.StudentDAO;
import dao.StudentGuardianDAO;
import dao.impl.GuardianDAOImpl;
import dao.impl.StudentDAOImpl;
import dao.impl.StudentGuardianDAOImpl;
import model.StudentGuardian;
import util.exception.ValidationException;

import java.util.List;
import java.util.Objects;

import static service.ServiceValidation.id;
import static service.ServiceValidation.relation;
import static service.ServiceValidation.require;

public class StudentGuardianService {

    private final StudentGuardianDAO relationDAO;
    private final StudentDAO studentDAO;
    private final GuardianService guardianService;

    public StudentGuardianService() {
        this(new StudentGuardianDAOImpl(), new StudentDAOImpl(), new GuardianDAOImpl());
    }

    public StudentGuardianService(StudentGuardianDAO relationDAO, StudentDAO studentDAO, GuardianDAO guardianDAO) {
        this.relationDAO = Objects.requireNonNull(relationDAO);
        this.studentDAO = Objects.requireNonNull(studentDAO);
        this.guardianService = new GuardianService(guardianDAO);
    }

    private void requireStudent(Integer studentId) {
        id(studentId);
        require(studentDAO.findById(studentId).isPresent(), "Estudiante no encontrado.");
    }

    public void link(StudentGuardian relation) {
        relation(relation);
        requireStudent(relation.getStudentId());
        guardianService.getById(relation.getGuardianId());
        require(relationDAO.findByIds(relation.getStudentId(), relation.getGuardianId()).isEmpty(),
                "El acudiente ya esta vinculado al estudiante.");

        List<StudentGuardian> existing = relationDAO.findByStudentId(relation.getStudentId());
        boolean primary = existing.isEmpty() || relation.isPrimary();
        require(!primary || existing.stream().noneMatch(StudentGuardian::isPrimary),
                "Ya hay un acudiente principal. Vincule primero y use el cambio de principal.");

        StudentGuardian newRelation = new StudentGuardian(
                relation.getGuardianId(),
                relation.getStudentId(),
                relation.getRelationShip(),
                primary,
                relation.isFinancialResponsible(),
                relation.isEmergencyContact()
        );
        relationDAO.link(newRelation);
        relation.setPrimary(primary);
    }

    public StudentGuardian getByIds(Integer studentId, Integer guardianId) {
        id(studentId);
        id(guardianId);
        return relationDAO.findByIds(studentId, guardianId)
                .orElseThrow(() -> new ValidationException("El acudiente no esta vinculado al estudiante."));
    }

    public List<StudentGuardian> findByStudentId(Integer studentId) {
        requireStudent(studentId);
        return relationDAO.findByStudentId(studentId);
    }

    public List<StudentGuardian> findByGuardianId(Integer guardianId) {
        guardianService.getById(guardianId);
        return relationDAO.findByGuardianId(guardianId);
    }

    public List<StudentGuardian> findFinancialResponsibles(Integer studentId) {
        requireStudent(studentId);
        return relationDAO.findFinancialResponsibles(studentId);
    }

    public void changePrimary(Integer studentId, Integer guardianId) {
        StudentGuardian existing = getByIds(studentId, guardianId);
        if (!existing.isPrimary()) {
            relationDAO.changePrimary(studentId, guardianId);
        }
    }

    public void updateRelation(StudentGuardian relation) {
        relation(relation);
        StudentGuardian existing = getByIds(relation.getStudentId(), relation.getGuardianId());
        require(existing.isPrimary() == relation.isPrimary(), "Use la operacion de cambio de principal.");
        relationDAO.updateRelation(relation);
    }

    public void unlink(Integer studentId, Integer guardianId) {
        StudentGuardian existing = getByIds(studentId, guardianId);
        require(relationDAO.findByStudentId(studentId).size() > 1,
                "El estudiante debe conservar al menos un acudiente.");
        require(!existing.isPrimary(), "Seleccione otro acudiente principal antes de desvincular este.");
        relationDAO.unlink(studentId, guardianId);
    }
}
