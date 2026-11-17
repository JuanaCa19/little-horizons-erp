package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record DebtReport(Integer studentId,String studentName,long pendingCharges,BigDecimal pendingBase,long overdueCharges,BigDecimal overdueBase,LocalDate earliestDueDate) {

}
