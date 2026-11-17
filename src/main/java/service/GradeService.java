package service;

import dao.GradeDAO;
import dao.impl.GradeDAOImpl;
import model.Grade;
import util.exception.ValidationException;

import java.util.List;
import java.util.Objects;

import static service.ServiceValidation.id;

public class GradeService {

    private final GradeDAO gradeDAO;

    public GradeService() {
        this(new GradeDAOImpl());
    }

    public GradeService(GradeDAO gradeDAO) {
        this.gradeDAO = Objects.requireNonNull(gradeDAO);
    }

    public Grade getById(Integer gradeId) {
        id(gradeId);
        return gradeDAO.findById(gradeId)
                .orElseThrow(() -> new ValidationException("Grado no encontrado."));
    }

    public List<Grade> findAll() {
        return gradeDAO.findAll();
    }
}
