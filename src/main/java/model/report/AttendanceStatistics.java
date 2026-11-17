package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record AttendanceStatistics(Integer studentId,String studentName,long present,long absent,long late,long excused) {
    public long total() { return present+absent+late+excused; }
    public BigDecimal attendanceRate() {
        long denominator=present+absent+late;
        return denominator==0 ? null : BigDecimal.valueOf(present+late).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(denominator),2,RoundingMode.HALF_UP);
    }
}
