package controller;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import model.*;
import model.enums.*;
import service.SubjectService;

public class SubjectController {
    private final SubjectService service;
    public SubjectController() { this(new SubjectService()); }
    public SubjectController(SubjectService service) { this.service=Objects.requireNonNull(service); }

    public int create(Subject value) { return service.create(value); }

    public Subject getById(Integer id) { return service.getById(id); }

    public List<Subject> findAll() { return service.findAll(); }

    public List<Subject> findByGrade(Integer gradeId) { return service.findByGrade(gradeId); }

    public void update(Subject value) { service.update(value); }
}
