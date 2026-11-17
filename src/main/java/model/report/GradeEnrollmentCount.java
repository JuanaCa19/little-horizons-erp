package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record GradeEnrollmentCount(Integer gradeId,String gradeName,long total,long active,long completed,long withdrawn) {

}
