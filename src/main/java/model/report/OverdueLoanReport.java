package model.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import model.enums.BorrowerType;

public record OverdueLoanReport(Integer loanId,BorrowerType borrowerType,Integer borrowerId,String borrowerName,Integer resourceId,String resourceName,LocalDate expectedReturnDate,long overdueDays) {

}
