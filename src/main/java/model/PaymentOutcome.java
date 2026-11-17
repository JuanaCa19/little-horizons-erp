package model;

import java.math.BigDecimal;
import java.util.List;

public record PaymentOutcome(List<Payment> payments) {
    public PaymentOutcome { payments=List.copyOf(payments); }
    public BigDecimal total() {
        return payments.stream().map(Payment::getAmount).reduce(new BigDecimal("0.00"),BigDecimal::add);
    }
}
