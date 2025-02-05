package com.portfolio.recruitment.currencyaccount.api.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ExchangeControllerTest {
/*
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
        // Given: Create an account with initial value and obtain its ID
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Jakub",
                "Skawiński",
                new BigDecimal("9984.14")
        );
        MvcResult result = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();
        long id = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

        // Given: Prepare an exchange request to convert PLN to USD
        ExchangeRequest exchangeRequest = new ExchangeRequest(
                id,
                new BigDecimal("100.00"),
                Currency.getInstance("PLN")
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
                                    "newBalancePLN": 9884.14,
                                    "newBalanceUSD": 23.84
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
                            "balancePLN": 9884.14,
                            "balanceUSD": 23.84
                        }""", id)));
    }

    @Test
    void testUSDtoPLNConversion() throws Exception {
        // Given: Create an account with initial value and obtain its ID
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Koszałek",
                "Opałek",
                new BigDecimal("9984.14")
        );
        MvcResult result = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();

        long id = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

        // Given: First, exchange 100.00 PLN to USD
        ExchangeRequest exchangeToUSDRequest = new ExchangeRequest(
                id,
                new BigDecimal("100.00"),
                Currency.getInstance("PLN")
        );
        mockMvc
                .perform(post("/api/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exchangeToUSDRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                {
                                    "newBalancePLN": 9884.14,
                                    "newBalanceUSD": 23.84
                                }
                        """));
        // Given: Then, prepare a request to convert 23.96 USD back to PLN
        ExchangeRequest exchangeToPLNRequest = new ExchangeRequest(
                id,
                new BigDecimal("23.84"),
                Currency.getInstance("USD"));
        // When: Perform the second exchange POST request to convert USD to PLN
        mockMvc
                .perform(post("/api/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exchangeToPLNRequest)))
                // Then: Expect status OK with updated balances in the response
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                {
                                    "newBalancePLN": 9982.18,
                                    "newBalanceUSD": 0.0
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
                            "balancePLN": 9982.18,
                            "balanceUSD": 0.0
                        }""", id)));
    }

    @Test
    void testInsuffisientAmount() throws Exception {
        // Given: Create an account with insufficient value
        AccountCreationRequest createAccountRequest = new AccountCreationRequest(
                "Koszałek",
                "Opałek",
                new BigDecimal("15.14")
        );
        MvcResult result = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isOk()).andReturn();
        long id = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

        // Given: Prepare an exchange request with an amount exceeding the value
        ExchangeRequest exchangeRequest = new ExchangeRequest(
                id,
                new BigDecimal("100.00"),
                Currency.getInstance("PLN"));
        // When: Perform the exchange POST request
        mockMvc
                .perform(post("/api/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exchangeRequest)))
                // Then: Expect status Bad Request and an error message with "Insufficient value"
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Insufficient value in PLN")));
    }


    @Test
    void testInvalidAmount() throws Exception {
        // Given: Prepare an exchange request with an invalid, negative amount
        ExchangeRequest invalidRequest = new ExchangeRequest(
                1L,
                new BigDecimal("-500.00"),
                Currency.getInstance("PLN")
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

    }*/
}