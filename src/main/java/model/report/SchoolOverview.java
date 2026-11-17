package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record SchoolOverview(long totalStudents,long activeStudents,long activeTeachers,long activeClubs,long totalResources,long availableResources,long openLoans,long overdueLoans) {

}
