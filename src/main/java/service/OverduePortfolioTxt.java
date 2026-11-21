package service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import model.report.OverdueChargeRow;
import util.AppLogger;
import util.exception.DataAccessException;

final class OverduePortfolioTxt {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private record Grade(Integer id, String name) {
    }

    private record Student(int id, String name) {
    }

    private OverduePortfolioTxt() {
    }

    static Path write(Path directory, LocalDate cutoff, List<OverdueChargeRow> charges) {
        String content = render(cutoff, charges);
        try {
            Files.createDirectories(directory);
            Path file = Files.createTempFile(directory, "cartera-vencida-" + cutoff + "-", ".txt");
            try {
                return Files.writeString(file, content, StandardCharsets.UTF_8).toAbsolutePath();
            } catch (IOException error) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException cleanupError) {
                    error.addSuppressed(cleanupError);
                }
                throw error;
            }
        } catch (IOException error) {
            AppLogger.logError("Exportación de cartera vencida", error);
            throw new DataAccessException("No se pudo guardar el reporte TXT de cartera vencida.", error);
        }
    }

    private static String render(LocalDate cutoff, List<OverdueChargeRow> charges) {
        StringBuilder text = new StringBuilder("""
                COLEGIO LITTLE HORIZONS
                REPORTE DE CARTERA VENCIDA POR GRADO Y ESTUDIANTE
                """);
        text.append("Fecha de corte: ").append(DATE.format(cutoff)).append('\n');
        text.append("Vencimientos anteriores al ").append(DATE.format(cutoff.minusMonths(1))).append('\n');
        text.append("""
                Importes en COP sin IVA. Pagos vinculados hasta la fecha de corte.
                El sistema registra liquidaciones completas por cargo.
                Se excluyen los cargos actualmente anulados. El grado corresponde al año del cargo.

                """);

        if (charges.isEmpty()) {
            text.append("No se encontraron cargos en mora para esta fecha de corte.\n\n");
        }
        var byGrade = charges.stream().collect(Collectors.groupingBy(
                row -> new Grade(row.gradeId(), row.gradeName()), LinkedHashMap::new, Collectors.toList()));
        byGrade.forEach((grade, gradeCharges) -> {
            text.append("Grado: ").append(label(grade.name())).append('\n');
            var byStudent = gradeCharges.stream().collect(Collectors.groupingBy(
                    row -> new Student(row.studentId(), row.studentName()),
                    LinkedHashMap::new, Collectors.toList()));
            byStudent.forEach((student, studentCharges) -> {
                text.append("  Estudiante ").append(student.id()).append(" | ")
                        .append(label(student.name())).append('\n');
                for (OverdueChargeRow charge : studentCharges) {
                    text.append("    Cargo ").append(charge.chargeId()).append(" | ")
                            .append(label(charge.concept())).append(" | Año ").append(charge.academicYear())
                            .append(" | Venció ").append(DATE.format(charge.dueDate()))
                            .append(" | Monto base ").append(charge.balance().toPlainString())
                            .append(" | Pagado al corte 0.00 | Saldo ").append(charge.balance().toPlainString())
                            .append('\n');
                }
                text.append("  Subtotal del estudiante: ").append(total(studentCharges).toPlainString()).append('\n');
            });
            text.append("Subtotal del grado: ").append(total(gradeCharges).toPlainString()).append("\n\n");
        });
        text.append("Cargos en mora: ").append(charges.size()).append('\n');
        return text.append("Total general (COP): ").append(total(charges).toPlainString()).append('\n').toString();
    }

    private static BigDecimal total(List<OverdueChargeRow> charges) {
        return charges.stream().map(OverdueChargeRow::balance)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    private static String label(String value) {
        return value.replaceAll("\\s+", " ").strip();
    }
}
