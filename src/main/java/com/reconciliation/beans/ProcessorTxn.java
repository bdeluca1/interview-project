package com.reconciliation.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@ToString
public class ProcessorTxn {

    @JsonProperty("network_ref")
    private String networkRef;
    @JsonProperty("merchant_ref")
    private String merchRef;
    @JsonProperty("merchant_id")
    private String merchId;
    @JsonProperty("card_last4")
    private String cardLast4;
    @JsonProperty("card_type")
    private String cardType;
    @JsonProperty("settled_amount")
    private BigDecimal settledAmt;
    @JsonProperty("interchange_fee")
    private BigDecimal interchangeFee;
    @JsonProperty("processor_fee")
    private BigDecimal processorFee;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("settlement_date")
    private LocalDate settleDate;

    public BigDecimal getGrossAmt(){
        return settledAmt.add(interchangeFee).add(processorFee);
    }


}
