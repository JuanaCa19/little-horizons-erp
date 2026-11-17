package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record SubjectAverageReport(Integer subjectId,String subjectName,String gradeName,String section,BigDecimal average,long gradedStudents) {

}
