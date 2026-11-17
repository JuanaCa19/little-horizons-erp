package view;

import controller.GuardianController;
import controller.StudentController;
import controller.StudentGuardianController;
import model.Guardian;
import model.Student;
import model.StudentGuardian;

import java.util.List;
import java.util.Objects;

import static util.ConsoleUI.printHeader;
import static util.ConsoleUI.printInfo;
import static util.ConsoleUI.printSuccess;
import static util.ConsoleUI.printTable;
import static util.InputReader.readRequiredText;
import static util.InputReader.readYesNo;
import static view.ViewSupport.action;
import static view.ViewSupport.field;
import static view.ViewSupport.menu;
import static view.ViewSupport.readId;
import static view.ViewSupport.row;

public class StudentGuardianView {

    private final StudentGuardianController controller;
    private final StudentController students;
    private final GuardianController guardians;

    public StudentGuardianView() {
        this(new StudentGuardianController(), new StudentController(), new GuardianController());
    }

    public StudentGuardianView(StudentGuardianController controller, StudentController students,
                               GuardianController guardians) {
        this.controller = Objects.requireNonNull(controller);
        this.students = Objects.requireNonNull(students);
        this.guardians = Objects.requireNonNull(guardians);
    }

    public void start() {
        menu("Vinculos con Acudientes", "Volver",
                new String[]{
                        "Vincular acudiente existente",
                        "Acudientes del estudiante",
                        "Estudiantes del acudiente",
                        "Responsables financieros",
                        "Cambiar acudiente principal",
                        "Actualizar vinculo",
                        "Desvincular acudiente"
                },
                action(this::link),
                action(this::listGuardians),
                action(this::listStudents),
                action(this::financialResponsibles),
                action(this::changePrimary),
                action(this::update),
                action(this::unlink)
        );
    }

    public StudentGuardian readForm(Integer studentId, Integer guardianId, boolean primary) {
        StudentGuardian relation = new StudentGuardian();
        relation.setStudentId(studentId);
        relation.setGuardianId(guardianId);
        relation.setRelationShip(readRequiredText("Parentesco: "));
        relation.setPrimary(primary);
        relation.setFinancialResponsible(readYesNo("Es responsable financiero"));
        relation.setEmergencyContact(readYesNo("Es contacto de emergencia"));
        return relation;
    }

    private int selectStudent() {
        Student student = students.getById(readId("ID del estudiante"));
        field("Estudiante", student.getFullName());
        return student.getId();
    }

    private int selectGuardian() {
        Guardian guardian = guardians.getById(readId("ID del acudiente"));
        field("Acudiente", guardian.getFullName());
        return guardian.getId();
    }

    private void link() {
        int studentId = selectStudent();
        int guardianId = selectGuardian();
        boolean first = controller.findByStudentId(studentId).isEmpty();

        printInfo(first ? "El primer acudiente sera principal."
                : "Puede cambiar el principal desde la opcion correspondiente.");

        StudentGuardian relation = readForm(studentId, guardianId, first);
        showDetails(relation);

        if (readYesNo("Guardar vinculo")) {
            controller.link(relation);
            printSuccess("Acudiente vinculado.");
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void listGuardians() {
        showRelations(controller.findByStudentId(selectStudent()));
    }

    private void listStudents() {
        showRelations(controller.findByGuardianId(selectGuardian()));
    }

    private void financialResponsibles() {
        showRelations(controller.findFinancialResponsibles(selectStudent()));
    }

    private void showRelations(List<StudentGuardian> relations) {
        printTable(
                List.of("Estudiante ID", "Estudiante", "Acudiente ID", "Acudiente",
                        "Parentesco", "Principal", "Financiero", "Emergencia"),
                relations.stream()
                        .map(relation -> row(
                                relation.getStudentId(),
                                students.getById(relation.getStudentId()).getFullName(),
                                relation.getGuardianId(),
                                guardians.getById(relation.getGuardianId()).getFullName(),
                                relation.getRelationShip(),
                                relation.isPrimary(),
                                relation.isFinancialResponsible(),
                                relation.isEmergencyContact()
                        ))
                        .toList()
        );
    }

    private StudentGuardian selectRelation() {
        int studentId = selectStudent();
        showRelations(controller.findByStudentId(studentId));
        return controller.getByIds(studentId, selectGuardian());
    }

    private void changePrimary() {
        StudentGuardian relation = selectRelation();
        if (readYesNo("Establecer este acudiente como principal")) {
            controller.changePrimary(relation.getStudentId(), relation.getGuardianId());
            printSuccess("Acudiente principal actualizado.");
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void update() {
        StudentGuardian existing = selectRelation();
        showDetails(existing);
        StudentGuardian updated = readForm(existing.getStudentId(), existing.getGuardianId(), existing.isPrimary());
        showDetails(updated);

        if (readYesNo("Guardar cambios del vinculo")) {
            controller.updateRelation(updated);
            printSuccess("Vinculo actualizado.");
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void unlink() {
        StudentGuardian relation = selectRelation();
        showDetails(relation);
        if (readYesNo("Desvincular este acudiente del estudiante")) {
            controller.unlink(relation.getStudentId(), relation.getGuardianId());
            printSuccess("Acudiente desvinculado.");
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    public static void showDetails(StudentGuardian relation) {
        printHeader("Detalle del vinculo");
        field("Estudiante ID", relation.getStudentId());
        field("Acudiente ID", relation.getGuardianId());
        field("Parentesco", relation.getRelationShip());
        field("Principal", relation.isPrimary());
        field("Responsable financiero", relation.isFinancialResponsible());
        field("Contacto de emergencia", relation.isEmergencyContact());
    }
}
