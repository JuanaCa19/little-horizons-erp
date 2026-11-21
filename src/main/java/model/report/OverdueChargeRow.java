package model.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueChargeRow(int chargeId, int studentId, String studentName,
        Integer gradeId, String gradeName, int academicYear, String concept,
        LocalDate dueDate, BigDecimal balance) {
}
