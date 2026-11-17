package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record IncomeReport(Integer conceptId,String conceptName,long payments,BigDecimal total) {

}
