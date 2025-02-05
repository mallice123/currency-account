package com.portfolio.recruitment.currencyaccount.api.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Currency;

public class CurrencySerializer extends JsonSerializer<Currency> {

    @Override
    public void serialize(Currency currency, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(currency.getCurrencyCode());
    }

}
