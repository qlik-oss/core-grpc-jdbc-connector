# Example JDBC gRPC Connector

[![CircleCI](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector.svg?style=shield)](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector)

This connector will exemplify how a JDBC gRPC Connector can be written. This connector contains an example that includes a MySQL Database, a PostgreSQL Database, a QIX Engine and the JDBC gRPC connector.

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

### Performance tips

The perfomance of the JDBC gRPC connector can be tweaked with a few different environment settings.

To limit the amount of memory consumed in the connector when fetching data from the database the `DATABASE_FETCH_SIZE` can be used.
If nothing is set then entire database query will be loaded into the memory of the connector.
If `DATABASE_FETCH_SIZE` is entered then that will set the amount rows fetched from the database loaded into memory in batches.
The default `DATABASE_FETCH_SIZE` is set to 100000.

Another performance tweak is the size of the Data Chunks sent over gRPC to QIX Engine.
The `MAX_DATA_CHUNK_SIZE` represent how many fields can be batched together in one package.
This setting is highly dependant on the content of the fields and the package should be keept below the default 4MB gRPC package size limit.
The default `MAX_DATA_CHUNK_SIZE` is set to 300

These settings can be changed in the example in [docker-compose.yml](/examples/docker-compose.yml) file.

## Run locally

### Requirements
- Java JDK 8.0
- Maven 3.5.3

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
