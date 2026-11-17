package view;

import controller.TeacherController;
import java.util.List;
import java.util.Objects;
import model.Teacher;
import model.enums.*;
import static util.ConsoleUI.*;
import static util.InputReader.*;
import static view.ViewSupport.*;

public class TeacherView {
    private static final DocumentType[] DOCUMENT_TYPES = {DocumentType.CITIZENSHIP_CARD, DocumentType.FOREIGNER_CARD,
            DocumentType.TEMPORARY_PERMIT, DocumentType.PASSPORT};
    private final TeacherController controller;
    private final TeacherAssignmentView assignments;
    public TeacherView() { this(new TeacherController(), new TeacherAssignmentView()); }
    public TeacherView(TeacherController controller, TeacherAssignmentView assignments) {
        this.controller = Objects.requireNonNull(controller);
        this.assignments = Objects.requireNonNull(assignments);
    }
    public void start() {
        menu("Gestion de Profesores", "Volver al menu principal", new String[]{"Registrar profesor", "Listar profesores",
                "Buscar por nombre", "Buscar por documento", "Consultar detalle", "Actualizar datos", "Cambiar estado",
                "Carga academica y horarios"}, action(this::create), action(() -> showList(controller.findAll())),
                action(() -> showList(controller.searchByName(readRequiredText("Nombre o apellido: ")))),
                action(this::findByDocument), action(() -> showDetails(select())), action(this::update),
                action(this::changeStatus), assignments::start);
    }
    private Teacher select() {
        Teacher teacher = controller.getById(readId("ID del profesor"));
        field("Profesor", teacher.getFullName());
        return teacher;
    }
    private Teacher readForm(Teacher current) {
        printHeader(current == null ? "Datos del profesor" : "Actualizar profesor");
        if (current != null) printInfo("Enter conserva los textos actuales. - borra los campos opcionales.");
        Teacher teacher = new Teacher();
        teacher.setId(current == null ? null : current.getId());
        teacher.setStatus(current == null ? TeacherStatus.ACTIVE : current.getStatus());
        teacher.setDocumentType(choose("Tipo de documento", DOCUMENT_TYPES, DocumentType::getDbValue,
                current == null ? null : current.getDocumentType()));
        teacher.setDocumentNumber(required("Numero de documento", current == null ? null : current.getDocumentNumber()));
        teacher.setFirstName(required("Primer nombre", current == null ? null : current.getFirstName()));
        teacher.setMiddleName(optional("Segundo nombre", current == null ? null : current.getMiddleName()));
        teacher.setFirstLastName(required("Primer apellido", current == null ? null : current.getFirstLastName()));
        teacher.setSecondLastName(optional("Segundo apellido", current == null ? null : current.getSecondLastName()));
        teacher.setEmail(required("Correo electronico", current == null ? null : current.getEmail()));
        teacher.setPhone(optional("Telefono", current == null ? null : current.getPhone()));
        teacher.setSpecialization(optional("Especializacion", current == null ? null : current.getSpecialization()));
        teacher.setHiringDate(date("Fecha de contratacion", current == null ? null : current.getHiringDate()));
        return teacher;
    }
    private void create() {
        Teacher teacher = readForm(null);
        showDetails(teacher);
        if (readYesNo("Guardar profesor")) printSuccess("Profesor registrado. ID: " + controller.create(teacher));
        else printInfo("No se guardaron cambios.");
    }
    private void update() {
        Teacher teacher = readForm(select());
        showDetails(teacher);
        if (readYesNo("Guardar cambios")) {
            controller.update(teacher);
            printSuccess("Profesor actualizado.");
        } else printInfo("No se guardaron cambios.");
    }
    private void changeStatus() {
        Teacher teacher = select();
        field("Estado actual", teacher.getStatus().getDbValue());
        TeacherStatus status = choose("Nuevo estado", TeacherStatus.values(), TeacherStatus::getDbValue, null);
        printInfo("Las asignaciones e historial existentes se conservaran.");
        if (readYesNo("Confirmar cambio de estado")) {
            controller.updateStatus(teacher.getId(), status);
            printSuccess("Estado actualizado.");
        } else printInfo("No se guardaron cambios.");
    }
    private void findByDocument() {
        DocumentType type = choose("Tipo de documento", DOCUMENT_TYPES, DocumentType::getDbValue, null);
        controller.findByDocument(readRequiredText("Documento: "), type)
                .ifPresentOrElse(TeacherView::showDetails, () -> printInfo("Profesor no encontrado."));
    }
    public static void showList(List<Teacher> teachers) {
        printTable(List.of("ID", "Nombre", "Documento", "Correo", "Especializacion", "Estado"), teachers.stream()
                .map(teacher -> row(teacher.getId(), teacher.getFullName(), teacher.getDocumentNumber(), teacher.getEmail(),
                        teacher.getSpecialization(), teacher.getStatus().getDbValue())).toList());
    }
    public static void showDetails(Teacher teacher) {
        printHeader("Detalle del profesor");
        field("ID", teacher.getId()); field("Nombre", teacher.getFullName());
        field("Tipo de documento", teacher.getDocumentType().getDbValue()); field("Documento", teacher.getDocumentNumber());
        field("Correo", teacher.getEmail()); field("Telefono", teacher.getPhone());
        field("Especializacion", teacher.getSpecialization()); field("Contratacion", teacher.getHiringDate());
        field("Estado", teacher.getStatus().getDbValue());
    }
}
