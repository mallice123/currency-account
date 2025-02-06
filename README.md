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

*   1. Creating an account by calling /api/account using a payload and verifying the response to confirm it returns an ID
*   2. With the returned ID, can we search and retrieve the account information of the client we just created? (This confirms that the returned ID is usable further in the process)
*   3. Throwing an exception when no initial balance is provided while creating an account
*   4. Throwing an exception when no first or last name is provided while creating an account

*   1. Exchanging currency from PLN to USD (to do this, the account must exist. Remember that the account's starting balance only concerns PLN)
*   2. Exchanging currency from USD to PLN (to do this, the user must create an account first and exchange PLN to USD, as there are no USD funds available otherwise)
*   After performing exchange steps 1 and 2, verify that the account balance has been updated accordingly
*   3. Check what happens when a request includes a currency that is not supported and throw an exception in such cases

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
