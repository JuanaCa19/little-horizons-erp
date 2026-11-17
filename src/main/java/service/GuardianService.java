package service;

import dao.GuardianDAO;
import dao.impl.GuardianDAOImpl;
import model.Guardian;
import model.enums.DocumentType;
import util.exception.ValidationException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static service.ServiceValidation.guardian;
import static service.ServiceValidation.id;
import static service.ServiceValidation.require;
import static service.ServiceValidation.text;

public class GuardianService {

    private final GuardianDAO guardianDAO;

    public GuardianService() {
        this(new GuardianDAOImpl());
    }

    public GuardianService(GuardianDAO guardianDAO) {
        this.guardianDAO = Objects.requireNonNull(guardianDAO);
    }

    public int create(Guardian guardian) {
        validateForCreation(guardian);
        int id = guardianDAO.create(guardian);
        guardian.setId(id);
        return id;
    }

    void validateForCreation(Guardian guardian) {
        guardian(guardian);
        require(guardian.getId() == null, "El nuevo acudiente no debe tener identificador.");
        require(!guardianDAO.existsByDocument(guardian.getDocumentType(), guardian.getDocumentNumber()),
                "Ya existe un acudiente con ese documento.");
    }

    public Guardian getById(Integer guardianId) {
        id(guardianId);
        return guardianDAO.findById(guardianId)
                .orElseThrow(() -> new ValidationException("Acudiente no encontrado."));
    }

    public Optional<Guardian> findByDocument(String documentNumber, DocumentType documentType) {
        require(documentType != null, "El tipo de documento es obligatorio.");
        return guardianDAO.findByDocument(text(documentNumber, "El documento"), documentType);
    }

    public List<Guardian> findAll() {
        return guardianDAO.findAll();
    }

    public List<Guardian> searchByName(String name) {
        return guardianDAO.searchByName(text(name, "El nombre"));
    }

    public void update(Guardian guardian) {
        guardian(guardian);
        getById(guardian.getId());
        guardianDAO.findByDocument(guardian.getDocumentNumber(), guardian.getDocumentType())
                .ifPresent(existing -> require(Objects.equals(existing.getId(), guardian.getId()),
                        "El documento pertenece a otro acudiente."));
        guardianDAO.update(guardian);
    }
}
