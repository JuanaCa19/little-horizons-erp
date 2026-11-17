package controller;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import model.Guardian;
import model.enums.DocumentType;
import service.GuardianService;

/**
 * Exposes guardian operations to views without console or persistence dependencies.
 * Validation and data access exceptions propagate to the calling view.
 */
public class GuardianController {
    private final GuardianService guardianService;

    public GuardianController() {
        this(new GuardianService());
    }

    public GuardianController(GuardianService guardianService) {
        this.guardianService = Objects.requireNonNull(guardianService);
    }

    public int create(Guardian guardian) {
        return guardianService.create(guardian);
    }

    public Guardian getById(Integer guardianId) {
        return guardianService.getById(guardianId);
    }

    public Optional<Guardian> findByDocument(String documentNumber, DocumentType documentType) {
        return guardianService.findByDocument(documentNumber, documentType);
    }

    public List<Guardian> findAll() {
        return guardianService.findAll();
    }

    public List<Guardian> searchByName(String name) {
        return guardianService.searchByName(name);
    }

    public void update(Guardian guardian) {
        guardianService.update(guardian);
    }
}
