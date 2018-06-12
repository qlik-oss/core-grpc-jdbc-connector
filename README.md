# Example JDBC gRPC Connector - Work in progress

[![CircleCI](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector.svg?style=shield)](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector)

This connector will exemplify how a JDBC gRPC Connector can be written. This connector contains an example that includes a PostgreSQL Database, a QIX Engine and the JDBC gRPC connector.

## Run example

Goto the examples folder and run:
```
ACCEPT_EULA=<yes/no> docker-compose up --build -d
```

Then goto the reload-runner directory and install NodeJs dependencies with:

```
npm install
```

To run the example:

```
npm start
```

To run integration tests:
```
npm test
```

## Run locally

### Requirements
- Java JDK 8.0
- Maven 3.5.3

```
mvn install
```

## Add other JDBC Drivers
Other JDBC Drivers can be added to the pom.xml file in the following section:

```
<dependencies>
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.2.2</version>
  </dependency>
</dependencies>
```

