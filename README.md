# Example JDBC gRPC Connector - Work in progress

[![CircleCI](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector.svg?style=shield)](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector)

- database_reader - reads the data from the database into reasonably sized SQL data chunks.
- async_translator - takes the SQL data chunks and translates them into GRPC data chunks.
- async_stream_writer - takes the GRPC data chunks and writes them onto the GRPC stream.

The reason for the division is to be able to utilize multiple CPU cores to process the different stages simultaneously.

### Requirements
- Java JDK 8.0 or higher
- Maven 3.5.3 or higher

### Run locally
```
mvn install
```

### Integration testing
Goto src/test/integration and run:
```
ACCEPT_EULA=<yes/no> docker-compose up --build -d
```
Goto reload-runner and run:
```
npm install && npm test
```
