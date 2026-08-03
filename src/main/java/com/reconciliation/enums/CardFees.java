package com.reconciliation.enums;

import java.math.BigDecimal;

public enum CardFees {
    VISA("0.0180", "0.10"),
    MASTERCARD("0.0190", "0.10"),
    AMEX("0.0250", "0.15"),
    DISCOVER("0.0200", "0.10"),
    PROC("0.003", "0.05");

    private final BigDecimal interchangeRate;
    private final BigDecimal interchangeFlat;

    CardFees(String interchangeRate, String interchangeFlat) {
        this.interchangeRate = new BigDecimal(interchangeRate);
        this.interchangeFlat = new BigDecimal(interchangeFlat);
    }

    public BigDecimal getInterchangeRate() {
        return interchangeRate;
    }

    public BigDecimal getInterchangeFlat() {
        return interchangeFlat;
    }

    public BigDecimal calculateInterchange(BigDecimal amount) {
        return amount.multiply(interchangeRate)
                .add(interchangeFlat);
    }
}
