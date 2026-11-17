package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record ResourceInventoryReport(Integer categoryId,String categoryName,long total,long available,long loaned,long maintenance,long retired) {

}
