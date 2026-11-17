package view;

import controller.GradeController;
import model.Grade;
import util.exception.CancelledException;

import java.util.List;
import java.util.Objects;

import static util.ConsoleUI.printError;
import static util.ConsoleUI.printInfo;
import static util.ConsoleUI.printTable;
import static view.ViewSupport.readId;
import static view.ViewSupport.row;

public class GradeView {

    private final GradeController controller;

    public GradeView() {
        this(new GradeController());
    }

    public GradeView(GradeController controller) {
        this.controller = Objects.requireNonNull(controller);
    }

    public void showAll() {
        printTable(
                List.of("ID", "Grado", "Nivel"),
                controller.findAll().stream()
                        .map(grade -> row(grade.getId(), grade.getName(), grade.getNumericLevel()))
                        .toList()
        );
    }

    public Grade select() {
        List<Grade> grades = controller.findAll();
        if (grades.isEmpty()) {
            printInfo("No hay grados disponibles.");
            throw new CancelledException();
        }

        printTable(
                List.of("ID", "Grado", "Nivel"),
                grades.stream()
                        .map(grade -> row(grade.getId(), grade.getName(), grade.getNumericLevel()))
                        .toList()
        );

        while (true) {
            int id = readId("ID del grado");
            for (Grade grade : grades) {
                if (grade.getId() == id) {
                    return grade;
                }
            }
            printError("Seleccione un grado de la lista.");
        }
    }
}
