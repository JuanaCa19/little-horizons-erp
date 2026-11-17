package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record TeacherWorkloadReport(Integer teacherId,String teacherName,String status,long assignments,long scheduledMinutes) {
    public BigDecimal scheduledHours() { return BigDecimal.valueOf(scheduledMinutes).divide(new BigDecimal("60"),2,RoundingMode.HALF_UP); }
}
