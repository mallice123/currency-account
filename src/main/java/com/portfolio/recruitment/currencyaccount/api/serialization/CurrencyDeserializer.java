package com.portfolio.recruitment.currencyaccount.api.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Currency;

public class CurrencyDeserializer extends JsonDeserializer<Currency> {

    @Override
    public Currency deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String currencyCode = parser.getText();
        return Currency.getInstance(currencyCode);
    }
}
