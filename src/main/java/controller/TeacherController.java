package controller;

import java.util.Objects;
import java.util.List;
import java.util.Optional;
import model.Teacher;
import model.enums.DocumentType;
import model.enums.TeacherStatus;
import service.TeacherService;

public class TeacherController {
    private final TeacherService service;
    public TeacherController() { this(new TeacherService()); }
    public TeacherController(TeacherService service) { this.service = Objects.requireNonNull(service); }

    public int create(Teacher teacher) { return service.create(teacher); }

    public Teacher getById(Integer id) { return service.getById(id); }

    public List<Teacher> findAll() { return service.findAll(); }

    public List<Teacher> searchByName(String name) { return service.searchByName(name); }

    public Optional<Teacher> findByDocument(String number, DocumentType type) { return service.findByDocument(number, type); }

    public void update(Teacher teacher) { service.update(teacher); }

    public void updateStatus(Integer id, TeacherStatus status) { service.updateStatus(id, status); }
}
