package model;

import java.math.BigDecimal;

public record ChargeQuote(StudentCharge charge,String studentName,String conceptName,BigDecimal vatRate,BigDecimal vat,BigDecimal total) { }
