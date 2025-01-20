# Stock Price Service

## Overview

The Stock Price Service is a microservice designed to monitor stock prices in real-time. 
It connects to the Finnhub WebSocket API to receive stock price updates and publishes these updates to a Kafka topic.

## Project Structure

The project is structured as follows:
- `client`: Contains the logic to connect to the Finnhub WebSocket API.
- `config`: Contains the application configuration to connect to Kafka and the client.
- `event`: Contains the event classes to represent an entry received from the Finnhub WebSocket API.
- `model`: Contains the model classes to represent the stock price, ticker, and trade data.
- `repository`: Contains the repository class to retrieve the monitored tickers from the database.
- `service`: Contains the main processing logic to handle the stock price updates and publish them to Kafka.

## Prerequisites

- Java 17
- Maven

## Configuration

The application configuration is managed in the [`application.yml`](src/main/resources/application.yml) file.
Remember to manage the sensitive information securely, using environment variables or an external configuration management tool.

### Configuration Parameters

- `spring.datasource.url`: The URL for connecting to the PostgreSQL database.
- `spring.datasource.username`: The username for the database.
- `spring.datasource.password`: The password for the database.
- `spring.jpa.hibernate.dialect`: The Hibernate dialect for the database.
- `spring.jpa.hibernate.ddl-auto`: The strategy for updating the database schema.
- `spring.kafka.bootstrap-servers`: The address of the Kafka server.
- `spring.kafka.template.default-topic`: The default Kafka topic for publishing messages.
- `finnhub.api-key`: The API key for authenticating with Finnhub.

### API Key Configuration

Replace `${FINNHUB_API_KEY}` with your Finnhub API key. You can set this key directly in the `application.yml` file or as an environment variable:

```sh
export FINNHUB_API_KEY=your_finnhub_api_key
```

### Database Configuration

Ensure that the database is running and that the `stock_price` database is created. 
You can adjust the `url`, `username`, and `password` parameters as needed to match your setup.

Your database must have a table named `ticker` with the following schema:

```sql
CREATE TABLE ticker (
    symbol VARCHAR(255) PRIMARY KEY
);
```

### Kafka Configuration

Ensure that Kafka is running and accessible at the address specified in `bootstrap-servers`. 
The default topic for publishing messages is `stock-price-updated`, but you can change it as needed.

## Usage

To run the application, use the following command:

```sh
./mvnw spring-boot:run
```

The application connects to the Finnhub WebSocket API and subscribes to the trade updates for the tickers stored in the database.
When a new stock price update is received, the application processes the data and publishes it to the Kafka topic.

The service does not impose a limit on the number of tickers you can monitor; 
however, your Finnhub subscription plan may restrict the number of tickers you can track.

The published message contains the following information:
- `symbol`: The ticker symbol for the stock.
- `price`: The current price of the stock. The currency depends on the exchange.
- `timestamp`: The timestamp when the price was received.

A sample message is shown below:

```json
{
  "symbol": "BINANCE:BTCUSDT",
  "price": 102960.89,
  "timestamp": "2025-01-01T00:00:00Z"
}
```

### Testing

To run the tests, use the following command:

```sh
./mvnw test
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.