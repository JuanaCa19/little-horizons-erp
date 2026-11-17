package model;

import java.math.BigDecimal;
import java.util.List;

public record PaymentOutcome(List<Payment> payments, List<ChargeQuote> charges) {
    public PaymentOutcome {
        payments = List.copyOf(payments);
        charges = List.copyOf(charges);
        if (payments.size() != charges.size()) {
            throw new IllegalArgumentException("Payment and charge counts must match.");
        }
    }

    public BigDecimal subtotal() {
        return charges.stream().map(quote -> quote.charge().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal vat() {
        return charges.stream().map(ChargeQuote::vat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal total() {
        return payments.stream().map(Payment::getAmount).reduce(new BigDecimal("0.00"),BigDecimal::add);
    }
}
