# Example JDBC gRPC Connector

[![CircleCI](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector.svg?style=shield)](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector)

This connector exemplifies how a JDBC gRPC Connector can be written. It contains an example that includes a MySQL Database, a PostgreSQL Database, a QIX Engine and the JDBC gRPC connector.

## Run example

Go to the examples folder and run the following:
```
ACCEPT_EULA=<yes/no> docker-compose up --build -d
```

Then go to the reload-runner directory and install NodeJs dependencies:

```
npm install
```

Use the following command to run the example:

```
npm start
```

To run integration tests, use the following command:
```
npm test
```

### Performance tips

The perfomance of the JDBC gRPC connector can be tweaked with a few different environment settings.

You can use the `DATABASE_FETCH_SIZE` command to limit the memory consumption in the connector when fetching data from the database.
`DATABASE_FETCH_SIZE` sets the amount of rows fetched from the database loaded into memory in batches.
The default `DATABASE_FETCH_SIZE` is 100000.
If `DATABASE_FETCH_SIZE` is not set, the entire database query is loaded into the memory of the connector.

You can use the `MAX_DATA_CHUNK_SIZE` command to tweak the size of the data chunks sent over gRPC to QIX Engine.
The `MAX_DATA_CHUNK_SIZE` represents how many fields can be batched together in one package.
This setting is highly dependant on the content of the fields and the package should be keept below the default 4MB gRPC package size limit.
The default `MAX_DATA_CHUNK_SIZE` is set to 300

These settings can be changed in the example in [docker-compose.yml](/examples/docker-compose.yml) file.

## Run locally

### Requirements
- Java JDK 8.0
- Maven 3.3.9

```
mvn install
java -jar ./target/core-grpc-jdbc-connector.jar
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


## License
This repository is licensed under [MIT](/LICENSE) but components used in the Dockerfile examples are under other licenses.
Make sure that you are complying with those licenses when using the built images.
