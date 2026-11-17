package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record ClubOccupancyReport(Integer clubId,String clubName,boolean active,int capacity,long activeMembers,long historicalStudents) {
    public long availableSeats() { return Math.max(0,capacity-activeMembers); }
    public BigDecimal occupancyRate() { return BigDecimal.valueOf(activeMembers).multiply(new BigDecimal("100")).divide(BigDecimal.valueOf(capacity),2,RoundingMode.HALF_UP); }
}
