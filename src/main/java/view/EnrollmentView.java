package view;

import controller.EnrollmentController;
import controller.StudentController;
import model.Enrollment;
import model.Student;
import model.enums.EnrollmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static util.ConsoleUI.printHeader;
import static util.ConsoleUI.printInfo;
import static util.ConsoleUI.printSuccess;
import static util.ConsoleUI.printTable;
import static util.InputReader.readDate;
import static util.InputReader.readDateInRange;
import static util.InputReader.readInt;
import static util.InputReader.readOptionalText;
import static util.InputReader.readYesNo;
import static view.ViewSupport.action;
import static view.ViewSupport.choose;
import static view.ViewSupport.field;
import static view.ViewSupport.menu;
import static view.ViewSupport.readId;
import static view.ViewSupport.row;

public class EnrollmentView {

    private final EnrollmentController controller;
    private final StudentController students;
    private final GradeView grades;

    public EnrollmentView() {
        this(new EnrollmentController(), new StudentController(), new GradeView());
    }

    public EnrollmentView(EnrollmentController controller, StudentController students, GradeView grades) {
        this.controller = Objects.requireNonNull(controller);
        this.students = Objects.requireNonNull(students);
        this.grades = Objects.requireNonNull(grades);
    }

    public void start() {
        menu("Gestion de Matriculas", "Volver",
                new String[]{
                        "Matricular estudiante",
                        "Historial por estudiante",
                        "Consultar por estudiante y anio",
                        "Listar por anio y grado",
                        "Consultar detalle",
                        "Cambiar estado",
                        "Consultar grados"
                },
                action(this::create),
                action(this::history),
                action(this::findByYear),
                action(this::listByGrade),
                action(() -> showDetails(controller.getById(readId("ID de matricula")))),
                action(this::changeStatus),
                action(grades::showAll)
        );
    }

    public Enrollment readForm(Integer studentId) {
        printHeader("Datos de matricula");
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setGradeId(grades.select().getId());
        enrollment.setAcademicYear(readInt("Anio academico: ", 1900, 9999));
        enrollment.setEnrollmentDate(readDate("Fecha de matricula (dd/MM/yyyy): "));
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setNotes(readOptionalText("Observaciones (opcional): "));
        return enrollment;
    }

    private Student selectStudent() {
        Student student = students.getById(readId("ID del estudiante"));
        field("Estudiante", student.getFullName());
        return student;
    }

    private void create() {
        Enrollment enrollment = readForm(selectStudent().getId());
        showDetails(enrollment);
        if (readYesNo("Guardar matricula")) {
            printSuccess("Matricula registrada. ID: " + controller.create(enrollment));
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    private void history() {
        showList(controller.findByStudentId(selectStudent().getId()));
    }

    private void findByYear() {
        int studentId = selectStudent().getId();
        controller.findByStudentAndYear(studentId, readInt("Anio academico: ", 1900, 9999))
                .ifPresentOrElse(EnrollmentView::showDetails, () -> printInfo("Matricula no encontrada."));
    }

    private void listByGrade() {
        int year = readInt("Anio academico: ", 1900, 9999);
        showList(controller.findByYearAndGrade(year, grades.select().getId()));
    }

    private void changeStatus() {
        Enrollment enrollment = controller.getById(readId("ID de matricula"));
        showDetails(enrollment);
        EnrollmentStatus status = choose("Nuevo estado", EnrollmentStatus.values(), EnrollmentStatus::getDbValue, null);

        LocalDate withdrawalDate = status == EnrollmentStatus.WITHDRAWN
                ? readDateInRange("Fecha de retiro (dd/MM/yyyy): ", enrollment.getEnrollmentDate(), LocalDate.now())
                : null;

        if (readYesNo("Confirmar cambio de estado")) {
            controller.updateStatus(enrollment.getId(), status, withdrawalDate);
            printSuccess("Estado de matricula actualizado.");
        } else {
            printInfo("No se guardaron cambios.");
        }
    }

    public static void showList(List<Enrollment> enrollments) {
        printTable(
                List.of("ID", "Estudiante ID", "Grado ID", "Anio", "Estado", "Matricula", "Retiro"),
                enrollments.stream()
                        .map(enrollment -> row(
                                enrollment.getId(),
                                enrollment.getStudentId(),
                                enrollment.getGradeId(),
                                enrollment.getAcademicYear(),
                                enrollment.getStatus().getDbValue(),
                                enrollment.getEnrollmentDate(),
                                enrollment.getWithdrawalDate()
                        ))
                        .toList()
        );
    }

    public static void showDetails(Enrollment enrollment) {
        printHeader("Detalle de matricula");
        field("ID", enrollment.getId());
        field("Estudiante ID", enrollment.getStudentId());
        field("Grado ID", enrollment.getGradeId());
        field("Anio academico", enrollment.getAcademicYear());
        field("Estado", enrollment.getStatus().getDbValue());
        field("Fecha de matricula", enrollment.getEnrollmentDate());
        field("Fecha de retiro", enrollment.getWithdrawalDate());
        field("Observaciones", enrollment.getNotes());
    }
}
