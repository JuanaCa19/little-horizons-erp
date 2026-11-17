package service;

import dao.*;
import dao.impl.*;
import java.util.*;
import model.Subject;
import util.exception.ValidationException;
import static service.ServiceValidation.*;

public class SubjectService {
    private final SubjectDAO subjects;
    private final GradeDAO grades;
    public SubjectService() { this(new SubjectDAOImpl(),new GradeDAOImpl()); }
    public SubjectService(SubjectDAO subjects,GradeDAO grades) {
        this.subjects=Objects.requireNonNull(subjects);this.grades=Objects.requireNonNull(grades);
    }
    private void validate(Subject subject) {
        require(subject!=null,"La materia es obligatoria.");
        subject.setCode(text(subject.getCode(),"El codigo").toUpperCase(Locale.ROOT));
        subject.setName(text(subject.getName(),"El nombre"));
        require(subject.getCode().length()<=20,"El codigo admite hasta 20 caracteres.");
        require(subject.getName().length()<=100,"El nombre admite hasta 100 caracteres.");
        require(subject.getWeeklyHours()>0,"Las horas semanales deben ser mayores que cero.");
        id(subject.getGradeId());require(grades.findById(subject.getGradeId()).isPresent(),"Grado no encontrado.");
        subjects.findByCode(subject.getCode()).ifPresent(existing -> require(subject.getId()!=null && Objects.equals(subject.getId(),existing.getId()),"El codigo ya pertenece a otra materia."));
        subjects.findByNameAndGrade(subject.getName(),subject.getGradeId()).ifPresent(existing -> require(subject.getId()!=null && Objects.equals(subject.getId(),existing.getId()),"Ya existe esa materia en el grado."));
    }
    public int create(Subject subject) {
        require(subject!=null && subject.getId()==null,"La nueva materia no debe tener identificador.");validate(subject);
        int id=subjects.create(subject);subject.setId(id);return id;
    }
    public Subject getById(Integer id) {
        ServiceValidation.id(id);return subjects.findById(id).orElseThrow(() -> new ValidationException("Materia no encontrada."));
    }
    public List<Subject> findAll() { return subjects.findAll(); }
    public List<Subject> findByGrade(Integer gradeId) {
        id(gradeId);return subjects.findAll().stream().filter(value -> Objects.equals(value.getGradeId(),gradeId)).toList();
    }
    public void update(Subject subject) {
        require(subject!=null,"La materia es obligatoria.");Subject existing=getById(subject.getId());validate(subject);
        require(Objects.equals(existing.getGradeId(),subject.getGradeId()) || !subjects.hasUsage(subject.getId()),
                "No se puede cambiar el grado de una materia con asignaciones o historial.");
        subjects.update(subject);
    }
}
