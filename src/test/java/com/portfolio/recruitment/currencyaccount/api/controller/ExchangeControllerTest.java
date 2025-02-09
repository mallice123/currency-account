package com.portfolio.recruitment.currencyaccount.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.jayway.jsonpath.JsonPath;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountCreationRequest;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountCurrencyUpdateRequest;
import com.portfolio.recruitment.currencyaccount.api.dto.ErrorResponse;
import com.portfolio.recruitment.currencyaccount.api.dto.ExchangeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.portfolio.recruitment.currencyaccount.business.service.util.CurrencyConstants.CURRENCY_CODE_PLN;
import static com.portfolio.recruitment.currencyaccount.business.service.util.CurrencyConstants.CURRENCY_CODE_USD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("integrationTest")
class ExchangeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:17.0")
                    .withUsername("test")
                    .withPassword("test")
                    .withDatabaseName("currency_account_db");

    private WireMockServer wireMockServer;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        registry.add("nbp.api.base.url", () -> "http://localhost:8081");
    }

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();

        mockExchangeRatesResponse();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void testPLNToUSDConversion() throws Exception {
        // Given: Create an account with initial initialValue and obtain its ID
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Jakub",
                "Skawiński",
                CURRENCY_CODE_PLN,
                new BigDecimal("1000.00")
        );
        MvcResult result = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();
        long id = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

        // Given: Add new currency type to balance
        AccountCurrencyUpdateRequest accountCurrencyUpdateRequest = new AccountCurrencyUpdateRequest(
                id,
                CURRENCY_CODE_USD,
                BigDecimal.ZERO
        );
        mockMvc.perform(patch("/api/account/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountCurrencyUpdateRequest)))
                .andExpect(status().isNoContent()).andReturn();

        // Given: Prepare an exchange request to convert PLN to USD
        ExchangeRequest exchangeRequest = new ExchangeRequest(
                id,
                new BigDecimal("400.00"),
                CURRENCY_CODE_PLN,
                CURRENCY_CODE_USD
        );

        // When: Perform a POST request to exchange currency
        mockMvc
                .perform(post("/api/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exchangeRequest)))
                // Then: Expect status OK with updated balances in the response
                .andExpect(status().isOk())
                .andExpect(content().json("""
                             {
                                 "currentBalance": [
                                     {
                                         "currencyCode": "PLN",
                                         "value": 600
                                     },
                                     {
                                         "currencyCode": "USD",
                                         "value": 95.34
                                     }
                                 ]
                             }
                        """));
        // When: Perform a GET request to retrieve the updated account
        mockMvc.perform(MockMvcRequestBuilders.get("/api/account/" + id))
                // Then: Expect the updated account details with new balances
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "id": %d,
                            "firstName": "Jakub",
                            "lastName": "Skawiński",
                            "currentBalance": [
                                 {
                                     "currencyCode": "PLN",
                                     "value": 600.00
                                 },
                                 {
                                 "currencyCode": "USD",
                                 "value": 95.34
                                 }]
                        }""", id)));
    }

    @Test
    void testUSDtoPLNConversion() throws Exception {
        // Given: Create an account with initial initialValue and obtain its ID
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Koszałek",
                "Opałek",
                CURRENCY_CODE_PLN,
                new BigDecimal("1000.00")

        );
        MvcResult result = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();

        long id = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

        // Given: Add new currency type to account
        AccountCurrencyUpdateRequest accountCurrencyUpdateRequest = new AccountCurrencyUpdateRequest(
                id,
                CURRENCY_CODE_USD,
                BigDecimal.valueOf(100.00)
        );
        mockMvc.perform(patch("/api/account/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountCurrencyUpdateRequest)))
                .andExpect(status().isNoContent()).andReturn();

        // Given: Then, prepare a request to convert USD back to PLN
        ExchangeRequest exchangeRequest = new ExchangeRequest(
                id,
                new BigDecimal("100.00"),
                CURRENCY_CODE_USD,
                CURRENCY_CODE_PLN);
        // When: Perform a POST request to exchange currency
        mockMvc
                .perform(post("/api/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exchangeRequest)))
                // Then: Expect status OK with updated balances in the response
                .andExpect(status().isOk())
                .andExpect(content().json("""
                             {
                                 "currentBalance": [
                                     {
                                         "currencyCode": "PLN",
                                         "value": 1411.24
                                     },
                                     {
                                         "currencyCode": "USD",
                                         "value": 0.00
                                     }
                                 ]
                             }
                        """));
        // When: Perform a GET request to retrieve the updated account
        mockMvc.perform(MockMvcRequestBuilders.get("/api/account/" + id))
                // Then: Expect the updated account details with new balances
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "id": %d,
                            "firstName": "Koszałek",
                            "lastName": "Opałek",
                            "currentBalance": [
                                 {
                                     "currencyCode": "PLN",
                                     "value": 1411.24
                                 },
                                 {
                                 "currencyCode": "USD",
                                 "value": 0.00
                                 }]
                        }""", id)));
    }

    @Test
    void testInsuffisientAmount() throws Exception {
        // Given: Create an account with insufficient initialValue
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Koszałek",
                "Opałek",
                CURRENCY_CODE_PLN,
                new BigDecimal("15.14")
        );
        MvcResult createAccountResult = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();
        long id = ((Number) JsonPath.read(createAccountResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Given: Prepare an exchange request with an amount exceeding the initialValue
        ExchangeRequest exchangeRequest = new ExchangeRequest(
                id,
                new BigDecimal("100.00"),
                CURRENCY_CODE_USD,
                CURRENCY_CODE_PLN);

        // Given: Add new currency type to account
        AccountCurrencyUpdateRequest accountCurrencyUpdateRequest = new AccountCurrencyUpdateRequest(
                id,
                CURRENCY_CODE_USD,
                BigDecimal.ZERO
        );
        mockMvc.perform(patch("/api/account/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountCurrencyUpdateRequest))).andReturn();
        // When: Perform the exchange POST request
        MvcResult exchangeResult = mockMvc
                .perform(post("/api/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exchangeRequest))).andReturn();
                // Then: Expect status Bad Request and an ErrorResponse
        ErrorResponse errorResponse = getErrorResponse(exchangeResult);
        assert errorResponse.status() == 400;
        assert errorResponse.message().equals("ILLEGAL_ARGUMENT");
        assert errorResponse.errorCode().equals("Insufficient initialValue in USD.");
        assert !errorResponse.details().isEmpty();
    }


    @Test
    void testInvalidAmount() throws Exception {
        // Given: Prepare an exchange request with an invalid, negative amount
        ExchangeRequest invalidRequest = new ExchangeRequest(
                1L,
                new BigDecimal("-500.00"),
                CURRENCY_CODE_PLN,
                CURRENCY_CODE_USD

        );
        // When: Perform the exchange POST request
        mockMvc
                .perform(post("/api/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                // Then: Expect status Bad Request
                .andExpect(status().isBadRequest());
    }

    private void mockExchangeRatesResponse() {
        wireMockServer.stubFor(get(urlPathMatching("/api/exchangerates/rates/C/USD"))
                .withQueryParam("format", matching("json"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "table": "C",
                                  "currency": "dolar amerykański",
                                  "code": "USD",
                                  "rates": [
                                    {
                                      "no": "011/C/NBP/2025",
                                      "effectiveDate": "2025-01-17",
                                      "bid": 4.1124,
                                      "ask": 4.1954
                                    }
                                  ]
                                }
                                """)
                        .withStatus(200))
        );
    }

    private ErrorResponse getErrorResponse(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
    }
}