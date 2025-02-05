package com.portfolio.recruitment.currencyaccount.api.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class AccountControllerTest {
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
                "Koszałek", "Opałek", new BigDecimal("500.00"));
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
                            "balancePLN": 500.00,
                            "balanceUSD": 0.00
                        }""", id)));
    }

    @Test
    public void testAccountCreation_MissingInitialBalance_ThrowsException() throws Exception {
        // Given: Prepare an account creation request with a missing initial value
        AccountCreationRequest invalidRequest = new AccountCreationRequest(
                "Jakub",
                "Skawiński",
                null
        );
        // When: Perform a POST request with the invalid request
        mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                // Then: Expect status Bad Request and proper error message
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Initial value cannot be null."));
    }

    @Test
    public void testAccountCreation_MissingFirstName_ThrowsException() throws Exception {
        // Given: Prepare an account creation request with a missing first name
        AccountCreationRequest invalidRequest = new AccountCreationRequest(
                null,
                "Skawiński",
                new BigDecimal("1000.00")
        );
        // When: Perform a POST request with the invalid request
        mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                // Then: Expect status Bad Request and proper error message
                .andExpect(status().isBadRequest())
                .andExpect(content().string("First name cannot be null."));
    }

    @Test
    public void testAccountCreation_MissingLastName_ThrowsException() throws Exception {
        // Given: Prepare an account creation request with a missing last name
        AccountCreationRequest invalidRequest = new AccountCreationRequest(
                "Jakub",
                null,
                new BigDecimal("1000.00")
        );
        // When: Perform a POST request with the invalid request
        mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                // Then: Expect status Bad Request and proper error message
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Last name cannot be null."));
    }
*/

}