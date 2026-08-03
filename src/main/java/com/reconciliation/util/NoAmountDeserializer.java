package com.reconciliation.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

public class NoAmountDeserializer
        extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(
            JsonParser parser,
            DeserializationContext context
    ) throws IOException {
        String value = parser.getValueAsString();

        if (value == null
                || value.isBlank()
                || value.equalsIgnoreCase("N/A")) {
            return null;
        }

        try {
            // Optional if amounts contain commas or currency symbols:
            value = value.replace(",", "").replace("$", "").trim();
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw context.weirdStringException(
                    value,
                    BigDecimal.class,
                    "Expected a numeric amount or N/A"
            );
        }
    }
}