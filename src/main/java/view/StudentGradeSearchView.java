package view;

import controller.StudentGradeSearchController;
import java.util.List;
import model.Grade;
import model.StudentEnrollmentSummary;

import static controller.StudentGradeSearchController.ACADEMIC_YEAR;
import static util.ConsoleUI.printHeader;
import static util.ConsoleUI.printInfo;
import static util.ConsoleUI.printTable;
import static util.InputReader.readInt;
import static view.ViewSupport.row;

final class StudentGradeSearchView {
    private final StudentGradeSearchController controller = new StudentGradeSearchController();

    void search() {
        printHeader("Buscar estudiantes por grado académico");
        List<Grade> grades = controller.findGrades();
        if (grades.isEmpty()) {
            printInfo("No hay grados académicos disponibles.");
            return;
        }

        for (int index = 0; index < grades.size(); index++) {
            System.out.printf("%d. %s%n", index + 1, grades.get(index).getName());
        }
        int option = readInt("Seleccione un grado (0 para cancelar): ", 0, grades.size());
        if (option == 0) {
            printInfo("Operación cancelada.");
            return;
        }

        Grade grade = grades.get(option - 1);
        List<StudentEnrollmentSummary> students = controller.searchByGrade(grade.getId());
        printHeader("Resultados de búsqueda");
        printInfo("Grado académico: " + grade.getName() + " | Año: " + ACADEMIC_YEAR);
        if (students.isEmpty()) {
            printInfo("No se encontraron estudiantes con matrícula activa en el grado seleccionado.");
            return;
        }

        printTable(List.of("ID", "Nombre", "Documento"), students.stream()
                .map(student -> row(student.id(), student.fullName(),
                        student.documentType() + " " + student.documentNumber()))
                .toList());
        printInfo("Total de resultados: " + students.size());
    }
}
