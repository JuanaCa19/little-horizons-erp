package view;

import java.util.Objects;

import static util.ConsoleUI.printInfo;
import static view.ViewSupport.menu;

public class MainMenuView {

    private final StudentView students;
    private final TeacherView teachers;
    private final AcademicView academics;
    private final ExtracurricularView extracurricular;
    private final FinanceView finance;
    private final ResourceView resources;
    private final ReportView reports;

    public MainMenuView() {
        this(new StudentView(), new TeacherView());
    }

    public MainMenuView(StudentView students) {
        this(students, new TeacherView());
    }

    public MainMenuView(StudentView students, TeacherView teachers) {
        this(students, teachers, new AcademicView());
    }

    public MainMenuView(StudentView students, TeacherView teachers, AcademicView academics) {
        this(students, teachers, academics, new ExtracurricularView());
    }

    public MainMenuView(StudentView students, TeacherView teachers, AcademicView academics,
                        ExtracurricularView extracurricular) {
        this(students, teachers, academics, extracurricular, new FinanceView());
    }

    public MainMenuView(StudentView students, TeacherView teachers, AcademicView academics,
                        ExtracurricularView extracurricular, FinanceView finance) {
        this(students, teachers, academics, extracurricular, finance, new ResourceView());
    }

    public MainMenuView(StudentView students, TeacherView teachers, AcademicView academics,
                        ExtracurricularView extracurricular, FinanceView finance, ResourceView resources) {
        this(students, teachers, academics, extracurricular, finance, resources, new ReportView());
    }

    public MainMenuView(StudentView students, TeacherView teachers, AcademicView academics,
                        ExtracurricularView extracurricular, FinanceView finance, ResourceView resources, ReportView reports) {
        this.reports = Objects.requireNonNull(reports);
        this.resources = Objects.requireNonNull(resources);
        this.finance = Objects.requireNonNull(finance);
        this.students = Objects.requireNonNull(students);
        this.teachers = Objects.requireNonNull(teachers);
        this.academics = Objects.requireNonNull(academics);
        this.extracurricular = Objects.requireNonNull(extracurricular);
    }

    public void start() {
        menu("ERP Colegio Little Horizons", "Salir",
                new String[]{
                        "Gestion de Estudiantes",
                        "Gestion Academica",
                        "Gestion de Profesores",
                        "Actividades Extracurriculares",
                        "Gestion Financiera",
                        "Gestion de Recursos",
                        "Reportes y Estadisticas"
                },
                students::start,
                academics::start,
                teachers::start,
                extracurricular::start,
                finance::start,
                resources::start,
                reports::start
        );
        printInfo("Hasta pronto.");
    }

}
