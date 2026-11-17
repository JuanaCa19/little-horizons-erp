package controller;

import java.util.List;
import java.util.Objects;
import model.Grade;
import service.GradeService;

/**
 * Exposes grade operations to views without console or persistence dependencies.
 * Validation and data access exceptions propagate to the calling view.
 */
public class GradeController {
    private final GradeService gradeService;

    public GradeController() {
        this(new GradeService());
    }

    public GradeController(GradeService gradeService) {
        this.gradeService = Objects.requireNonNull(gradeService);
    }

    public Grade getById(Integer gradeId) {
        return gradeService.getById(gradeId);
    }

    public List<Grade> findAll() {
        return gradeService.findAll();
    }
}
