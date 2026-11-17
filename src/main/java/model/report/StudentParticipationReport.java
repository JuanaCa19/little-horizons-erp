package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record StudentParticipationReport(Integer studentId,String studentName,long activeClubs,long historicalClubs,long events) {

}
