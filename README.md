## **Overview**

The `currency-account` application is a currency account management system designed for managing user accounts with balances in various currencies for polish citizens. It supports operations like creating user accounts, adding new balance type, performing currency exchanges between between PLN and USD, EUR, GBP
The application is built using **Java** with **Spring Boot** **Hibernate** frameworks, managed with **Docker** and **Docker Compose** for deployment, and uses **PostgreSQL** as the underlying database. Testing is integrated using **JUnit**, **Testcontainers**, and **WireMock** for mocking APIs.

## **Requirements**

To run this application, ensure you have the following installed on your system:
1. **Java Development Kit (JDK)**: Version 21+
2. **Maven**: Installed and available on your PATH (optional)
3. **Docker**: Version 20.x or higher
4. **Docker Compose**: Version 2.1 or higher

## **How to run**
git clone https://github.com/mallice123/currency-account

1. **Run the Application via Makefile from project root**
   make start

2. 1. **Access the Application**
    - The application will be running locally at: `http://localhost:8080`

The application is fully containerized. Locally all you need is to run **Docker Compose**.

**Start the docker Services** From the project root directory:
docker-compose up -d

## **Testing**
Unit and integration tests for this project are comprehensive and written using:
- **JUnit 5**
- **MockMvc** for controller-level tests
- **Testcontainers** for isolated database testing
- **WireMock** for mocking external APIs

mvn test

Integration tests covers following scenarios:

//TBA

## **Application Structure**
### Key Components:
1. **API Endpoints**:
   - `AccountController`: For handling account creation and account queries
   - `ExchangeController`: For managing currency exchanges with exchange rates fetched from `NBP API`
   - `BalanceController`: For managing existing account by adding new balance type for available products (EUR, USD, GBP)

2. **External Integration**:
   - `CurrencyExchangeClient`: Connects to [NBP API]() to fetch currency exchange rates
   - Implements caching (via Caffeine) and circuit breaker (via Resilience4j) for fault tolerance

3. **Configuration Files**:
   - `application.properties`: Uses environment variables for database and third-party API configurations
   - `.env`: Store environment-specific configurations for use with Docker and local setups
   - `Makefile`: Simplifies starting, stopping, and restarting the application during local development


## **Using the Application**
### Key Supported Operations:
1. **Account Creation**: Send a `POST` request to `http://localhost:8080/api/account`

   {
   "firstName": "John",
   "lastName": "Doe",
   "currencyCode": "PLN",
   "initialBalance": 1000.00
   }

To test the application you can use one of the following currency codes: USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY, SEK, NZD (there is more it's just an example)

3. **Get Account Details**: Send a `GET` request to `http://localhost:8080/api/account/{id}`:
{
   "id": 1,
   "firstName": "John",
   "lastName": "Doe",
   "CurrentBalance": [
      {
        "currencyCode": "PLN",
        "value": 1000.00
}

4. **Currency Exchange**: Send the amount of money you want to get by providing initial and target currency. Example: Send a `POST` request to `http://localhost:8081/api/exchange`:

{
  "id": 1,
  "amount": 500,
  "initialCurrency": "USD",
  "targetCurrency": "PLN"
}


5. **Adding new balance type to existing account ** http://localhost:8080/api/balance

{
  "id": 1,
  "currencyCode": "USD",
  "value": 500.12
}


### Running Tests ###
mvn test
