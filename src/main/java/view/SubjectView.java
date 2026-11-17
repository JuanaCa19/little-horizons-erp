package view;

import controller.SubjectController;
import java.util.*;
import model.Subject;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class SubjectView {
    private final SubjectController controller;
    private final GradeView grades;
    public SubjectView() { this(new SubjectController(),new GradeView()); }
    public SubjectView(SubjectController controller,GradeView grades) { this.controller=Objects.requireNonNull(controller);this.grades=Objects.requireNonNull(grades); }
    public void start() {
        menu("Catalogo de Materias","Volver",new String[]{"Registrar materia","Listar materias","Consultar por grado","Consultar detalle","Actualizar materia"},
                action(this::create),action(() -> showList(controller.findAll())),action(() -> showList(controller.findByGrade(grades.select().getId()))),
                action(() -> showDetails(select())),action(this::update));
    }
    public Subject select() {
        Subject subject=controller.getById(readId("ID de materia"));field("Materia",subject.getName());return subject;
    }
    private Subject readForm(Subject current) {
        Subject value=new Subject();value.setId(current==null ? null : current.getId());
        if(current!=null) printInfo("Enter conserva los textos actuales. - borra la descripcion.");
        value.setCode(required("Codigo",current==null ? null : current.getCode()));
        value.setName(required("Nombre",current==null ? null : current.getName()));
        value.setDescription(optional("Descripcion",current==null ? null : current.getDescription()));
        value.setGradeId(current!=null && !readYesNo("Cambiar grado ["+current.getGradeId()+"]") ? current.getGradeId() : grades.select().getId());
        value.setWeeklyHours(current!=null && !readYesNo("Cambiar horas semanales ["+current.getWeeklyHours()+"]") ? current.getWeeklyHours() : readInt("Horas semanales: ",1,Integer.MAX_VALUE));
        value.setActive(readYesNo("Materia activa"));return value;
    }
    private void create() {
        Subject value=readForm(null);showDetails(value);
        if(readYesNo("Guardar materia")) printSuccess("Materia creada. ID: "+controller.create(value));else printInfo("No se guardaron cambios.");
    }
    private void update() {
        Subject value=readForm(select());showDetails(value);
        if(readYesNo("Guardar cambios")) { controller.update(value);printSuccess("Materia actualizada."); } else printInfo("No se guardaron cambios.");
    }
    public static void showList(List<Subject> values) {
        printTable(List.of("ID","Codigo","Materia","Grado ID","Horas","Activa"),values.stream().map(value -> row(value.getId(),value.getCode(),value.getName(),value.getGradeId(),value.getWeeklyHours(),value.isActive())).toList());
    }
    public static void showDetails(Subject value) {
        printHeader("Detalle de materia");field("ID",value.getId());field("Codigo",value.getCode());field("Materia",value.getName());
        field("Descripcion",value.getDescription());field("Grado ID",value.getGradeId());field("Horas semanales",value.getWeeklyHours());field("Activa",value.isActive());
    }
}
