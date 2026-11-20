package view;

import controller.StudentController;
import model.Enrollment;
import model.Guardian;
import model.Student;
import model.StudentGuardian;
import model.enums.DocumentType;
import model.enums.Gender;
import model.enums.StudentStatus;

import java.util.List;
import java.util.Objects;

import static util.ConsoleUI.printHeader;
import static util.ConsoleUI.printInfo;
import static util.ConsoleUI.printSuccess;
import static util.ConsoleUI.printTable;
import static util.InputReader.readRequiredText;
import static util.InputReader.readYesNo;
import static view.ViewSupport.action;
import static view.ViewSupport.choose;
import static view.ViewSupport.date;
import static view.ViewSupport.field;
import static view.ViewSupport.menu;
import static view.ViewSupport.optional;
import static view.ViewSupport.readId;
import static view.ViewSupport.required;
import static view.ViewSupport.row;

public class StudentView {

    private final StudentController controller;
    private final GuardianView guardians;
    private final EnrollmentView enrollments;
    private final StudentGuardianView relations;
    private final StudentGradeSearchView gradeSearch = new StudentGradeSearchView();

    public StudentView() {
        this(new StudentController(), new GuardianView(), new EnrollmentView(), new StudentGuardianView());
    }

    public StudentView(StudentController controller, GuardianView guardians,
                       EnrollmentView enrollments, StudentGuardianView relations) {
        this.controller = Objects.requireNonNull(controller);
        this.guardians = Objects.requireNonNull(guardians);
        this.enrollments = Objects.requireNonNull(enrollments);
        this.relations = Objects.requireNonNull(relations);
    }

    public void start() {
        menu("Gestion de Estudiantes", "Volver al menu principal",
                new String[]{
                        "Registrar estudiante con matricula",
                        "Listar estudiantes",
                        "Buscar por nombre",
                        "Buscar por documento",
                        "Consultar detalle",
                        "Actualizar datos",
                        "Cambiar estado",
                        "Gestionar acudientes",
                        "Gestionar vinculos con acudientes",
                        "Gestionar matriculas",
                        "Buscar por grado académico"
                },
                action(this::register),
                action(() -> showList(controller.findAll())),
                action(() -> showList(controller.searchByName(readRequiredText("Nombre o apellido: ")))),
                action(this::findByDocument),
                action(() -> showDetails(select())),
                action(this::update),
                action(this::changeStatus),
                guardians::start,
                relations::start,
                enrollments::start,
                action(gradeSearch::search)
        );
    }

    private Student select() {
        Student student = controller.getById(readId("ID del estudiante"));
        field("Estudiante seleccionado", student.getFullName());
        return student;
    }

    private Student readForm(Student current) {
        printHeader(current == null ? "Datos del estudiante" : "Actualizar estudiante");
        if (current != null) {
            printInfo("Enter conserva los textos actuales. '-' borra los campos opcionales.");
        }

        Student student = new Student();
        student.setId(current == null ? null : current.getId());
        student.setStatus(current == null ? StudentStatus.ACTIVE : current.getStatus());
        student.setDocumentType(choose("Tipo de documento", DocumentType.values(), DocumentType::getDbValue,
                current == null ? null : current.getDocumentType()));
        student.setDocumentNumber(required("Numero de documento", current == null ? null : current.getDocumentNumber()));
        student.setFirstName(required("Primer nombre", current == null ? null : current.getFirstName()));
        student.setMiddleName(optional("Segundo nombre", current == null ? null : current.getMiddleName()));
        student.setFirstLastName(required("Primer apellido", current == null ? null : current.getFirstLastName()));
        student.setSecondLastName(optional("Segundo apellido", current == null ? null : current.getSecondLastName()));
        student.setBirthDate(date("Fecha de nacimiento", current == null ? null : current.getBirthDate()));
        student.setGender(choose("Genero", Gender.values(), Gender::getDbValue, current == null ? null : current.getGender()));
        student.setAddress(required("Direccion", current == null ? null : current.getAddress()));
        student.setPhone(optional("Telefono", current == null ? null : current.getPhone()));
        student.setEmail(optional("Correo electronico", current == null ? null : current.getEmail()));
        student.setBloodType(optional("Tipo de sangre", current == null ? null : current.getBloodType()));
        student.setAllergies(optional("Alergias", current == null ? null : current.getAllergies()));
        student.setMedicalConditions(optional("Condiciones medicas", current == null ? null : current.getMedicalConditions()));
        student.setAdmissionDate(date("Fecha de ingreso", current == null ? null : current.getAdmissionDate()));

        return student;
    }

    private void register() {
        Student student = readForm(null);
        boolean existingGuardian = readYesNo("Usar un acudiente ya registrado");
        Guardian guardian = existingGuardian ? guardians.select() : guardians.readForm(null);
        StudentGuardian relation = relations.readForm(null, guardian.getId(), true);
        Enrollment enrollment = enrollments.readForm(null);

        showDetails(student);
        GuardianView.showDetails(guardian);
        StudentGuardianView.showDetails(relation);
        EnrollmentView.showDetails(enrollment);

        if (readYesNo("Guardar estudiante, acudiente, vinculo y matricula")) {
            int id = controller.register(student, guardian, relation, enrollment);
            printSuccess("Estudiante registrado. ID: " + id + ". Matricula ID: " + enrollment.getId());
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void update() {
        Student student = readForm(select());
        showDetails(student);
        if (readYesNo("Guardar cambios del estudiante")) {
            controller.update(student);
            printSuccess("Estudiante actualizado.");
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void changeStatus() {
        Student student = select();
        field("Estado actual", student.getStatus().getDbValue());
        StudentStatus status = choose("Nuevo estado", StudentStatus.values(), StudentStatus::getDbValue, null);

        if (status == StudentStatus.WITHDRAWN || status == StudentStatus.GRADUATED) {
            printInfo("Cierre primero las matriculas activas desde 'Gestionar matriculas'.");
        }

        if (readYesNo("Confirmar cambio de estado")) {
            controller.updateStatus(student.getId(), status);
            printSuccess("Estado del estudiante actualizado.");
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void findByDocument() {
        DocumentType type = choose("Tipo de documento", DocumentType.values(), DocumentType::getDbValue, null);
        controller.findByDocument(readRequiredText("Numero de documento: "), type)
                .ifPresentOrElse(StudentView::showDetails, () -> printInfo("Estudiante no encontrado."));
    }

    public static void showList(List<Student> students) {
        printTable(
                List.of("ID", "Nombre", "Tipo", "Documento", "Estado", "Telefono"),
                students.stream()
                        .map(student -> row(
                                student.getId(),
                                student.getFullName(),
                                student.getDocumentType().getDbValue(),
                                student.getDocumentNumber(),
                                student.getStatus().getDbValue(),
                                student.getPhone()
                        ))
                        .toList()
        );
    }

    public static void showDetails(Student student) {
        printHeader("Detalle del estudiante");
        field("ID", student.getId());
        field("Nombre", student.getFullName());
        field("Tipo de documento", student.getDocumentType().getDbValue());
        field("Documento", student.getDocumentNumber());
        field("Nacimiento", student.getBirthDate());
        field("Genero", student.getGender().getDbValue());
        field("Direccion", student.getAddress());
        field("Telefono", student.getPhone());
        field("Correo", student.getEmail());
        field("Tipo de sangre", student.getBloodType());
        field("Alergias", student.getAllergies());
        field("Condiciones medicas", student.getMedicalConditions());
        field("Ingreso", student.getAdmissionDate());
        field("Estado", student.getStatus().getDbValue());
    }
}
