package com.portfolio.recruitment.currencyaccount.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.jayway.jsonpath.JsonPath;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountCreationRequest;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountCurrencyUpdateRequest;
import com.portfolio.recruitment.currencyaccount.api.dto.ErrorResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("integrationTest")
class BalanceControllerTest {

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
    public void addingNewCurrencyTypes() throws Exception {
        // Given: Create an account with initial initialValue and obtain its ID
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Jakub",
                "Skawiński",
                "PLN",
                new BigDecimal("1000.00")
        );
        MvcResult result = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();
        long id = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

        // Given: Add new currency to balance type
        AccountCurrencyUpdateRequest accountCurrencyUpdateRequest = new AccountCurrencyUpdateRequest(
                id,
                "EUR",
                BigDecimal.ZERO
        );
        mockMvc.perform(patch("/api/account/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountCurrencyUpdateRequest)))
                .andExpect(status().isNoContent()).andReturn();

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
                                     "value": 1000.00
                                 },
                                 {
                                 "currencyCode": "EUR",
                                 "value": 0.00
                                 }]
                        }""", id)));
    }

    @Test
    public void addingNotSupportedCurrency() throws Exception {
        // Given: Create an account with initial initialValue and obtain its ID
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Jakub",
                "Skawiński",
                "PLN",
                new BigDecimal("1000.00")
        );
        MvcResult accountCreationResult = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();
        long id = ((Number) JsonPath.read(accountCreationResult.getResponse().getContentAsString(), "$.id")).longValue();

        // Given: Add new currency to balance type
        AccountCurrencyUpdateRequest accountCurrencyUpdateRequest = new AccountCurrencyUpdateRequest(
                id,
                "KSH",
                BigDecimal.ZERO
        );
        MvcResult updateNewCurrencyResult = mockMvc.perform(patch("/api/account/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountCurrencyUpdateRequest)))
                .andReturn();

        // Then: Expect status Bad Request and an errorResponse
        ErrorResponse errorResponse = getErrorResponse(updateNewCurrencyResult);
        assert errorResponse.status() == 400;
        assert errorResponse.errorCode().equals("Provided currency is not supported: KSH");
        assert errorResponse.message().equals("CURRENCY_NOT_SUPPORTED");
        assert !errorResponse.details().isEmpty();
    }


    @Test
    public void addingMultipleSupportedCurrencies() throws Exception {
        // Given: Create an account with initial initialValue and obtain its ID
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Jakub",
                "Skawiński",
                "PLN",
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
                "EUR",
                BigDecimal.ZERO
        );
        mockMvc.perform(patch("/api/account/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountCurrencyUpdateRequest)))
                .andExpect(status().isNoContent()).andReturn();

        // Given: Add second currency type to account
        AccountCurrencyUpdateRequest secondAccountCurrencyUpdateRequest = new AccountCurrencyUpdateRequest(
                id,
                "USD",
                BigDecimal.valueOf(250.35)
        );
        mockMvc.perform(patch("/api/account/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondAccountCurrencyUpdateRequest)))
                .andExpect(status().isNoContent()).andReturn();
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
                                     "value": 1000.00
                                 },
                                 {
                                 "currencyCode": "EUR",
                                 "value": 0.00
                                 },
                                 {
                                 "currencyCode": "USD",
                                 "value": 250.35
                                 }]
                        }""", id)));
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