package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record StudentAverageReport(Integer studentId,String studentName,String gradeName,BigDecimal average,long gradedSubjects) {

}
