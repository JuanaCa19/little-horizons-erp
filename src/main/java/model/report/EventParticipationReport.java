package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record EventParticipationReport(Integer eventId,String eventName,LocalDate date,String type,boolean active,long participants,long competitors,long awards) {

}
