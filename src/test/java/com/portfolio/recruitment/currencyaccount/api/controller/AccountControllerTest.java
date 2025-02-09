package com.portfolio.recruitment.currencyaccount.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.jayway.jsonpath.JsonPath;
import com.portfolio.recruitment.currencyaccount.api.dto.AccountCreationRequest;
import com.portfolio.recruitment.currencyaccount.api.dto.ErrorResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class AccountControllerTest {
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
    }

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    public void testAccountCreation_Success() throws Exception {
        // Given: Prepare a valid account creation request
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Jakub",
                "Skawiński",
                "PLN",
                new BigDecimal("1000.00")
        );
        // When: Perform a POST request to create an account
        mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))

                // Then: Expect status OK and proper JSON response
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                {
                                  "id": 1
                                }
                        """));
    }

    @Test
    public void testGetAccountByObtainedId_Success() throws Exception {
        // Given: Create an account and obtain its ID
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Koszałek", "Opałek","PLN",  new BigDecimal("500.00"));
        MvcResult result = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();

        long id = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

       // When: Perform a GET request to retrieve the created account by ID
        mockMvc.perform(get("/api/account/" + id))
                // Then: Expect status OK and proper JSON response
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "id": %d,
                            "firstName": "Koszałek",
                            "lastName": "Opałek",
                            "currentBalance": [
                                 {
                                     "currencyCode": "PLN",
                                     "value": 500.00
                                 }]
                        }""", id)));
    }

    @Test
    public void testAccountCreation_MissingInitialBalance_ThrowsException() throws Exception {
        // Given: Prepare an account creation request with a missing initial initialValue
        AccountCreationRequest invalidRequest = new AccountCreationRequest(
                "Jakub",
                "Skawiński",
                null,
                new BigDecimal("1000.00")
        );
        // When: Perform a POST request with the invalid request
        MvcResult result = mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andReturn();

                // Then: Expect status Bad Request and ErrorResponse
        ErrorResponse errorResponse = getErrorResponse(result);
        assert errorResponse.status() == 400;
        assert errorResponse.errorCode().equals("Currency code cannot be null.");
        assert errorResponse.message().equals("VALIDATION_ERROR");
        assert errorResponse.details().size() == 1;
        assert errorResponse.details().getFirst().equals("Validation of failed");
    }

    @Test
    public void testAccountCreation_MissingFirstName_ThrowsException() throws Exception {
        // Given: Prepare an account creation request with a missing first name
        AccountCreationRequest invalidRequest = new AccountCreationRequest(
                null,
                "Skawiński",
                "PLN",
                new BigDecimal("1000.00")
        );
        // When: Perform a POST request with the invalid request
        MvcResult result = mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))).andReturn();

                // Then: Expect status Bad Request and proper error message
        ErrorResponse errorResponse = getErrorResponse(result);
        assert errorResponse.status() == 400;
        assert errorResponse.errorCode().equals("First name cannot be null.");
        assert errorResponse.message().equals("VALIDATION_ERROR");
        assert errorResponse.details().size() == 1;
        assert errorResponse.details().getFirst().equals("Validation of failed");
    }

    @Test
    public void testAccountCreation_MissingLastName_ThrowsException() throws Exception {
        // Given: Prepare an account creation request with a missing last name
        AccountCreationRequest invalidRequest = new AccountCreationRequest(
                "Jakub",
                null,
                "PLN",
                new BigDecimal("1000.00")
        );

        MvcResult result = mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))).andReturn();

        // Then: Expect status Bad Request and proper error response object
        ErrorResponse errorResponse = getErrorResponse(result);
        assert errorResponse.status() == 400;
        assert errorResponse.errorCode().equals("Last name cannot be null.");
        assert errorResponse.message().equals("VALIDATION_ERROR");
        assert errorResponse.details().size() == 1;
        assert errorResponse.details().getFirst().equals("Validation of failed");
    }

    private ErrorResponse getErrorResponse(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
    }

}