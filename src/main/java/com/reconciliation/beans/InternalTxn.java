package com.reconciliation.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.reconciliation.util.NoAmountDeserializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@ToString
public class InternalTxn {
    @JsonProperty("internal_txn_id")
    private String txnId;
    @JsonProperty("merchant_id")
    private String merchId;
    @JsonProperty("merchant_ref")
    private String merchRef;
    @JsonProperty("card_type")
    private String cardType;
    @JsonProperty("card_last4")
    private String cardLast4;
    @JsonProperty("gross_amount")
    @JsonDeserialize(using = NoAmountDeserializer.class)
    private BigDecimal grossAmt;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("type")
    private String type;
    @JsonProperty("captured_at")
    private Instant capturedDate;
}
