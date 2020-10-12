# Example JDBC gRPC Connector

[![CircleCI](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector.svg?style=shield)](https://circleci.com/gh/qlik-oss/core-grpc-jdbc-connector)

*As of 1 July 2020, Qlik Core is no longer available to new customers. No further maintenance will be done in this repository.*

This connector exemplifies how a JDBC gRPC Connector can be written. It contains examples that includes a MySQL Database, a PostgreSQL Database, a QIX Engine and the JDBC gRPC connector.

## Run examples

Go to the examples folder and run the following:

```bash
ACCEPT_EULA=<yes/no> docker-compose up --build -d
```

Then follow the instructions for either corectl or node.

### Node

Head into [/examples/node](/examples/node) and install the dependencies and then run the script [index.js](/examples/node/index.js) using the following commands:

```bash
npm install
npm start
```

To run integration tests you can use:

```bash
npm test
```

### Corectl

To run the corectl example head into [/examples/corectl](/examples/corectl). If you do not yet have corectl installed just follow the download instructions from [corectl](https://github.com/qlik-oss/corectl).

Once installed you can build using either the postgres or mysql database with the following commands, respectively:
```bash
corectl build --script mysql.qvs
corectl build --script postgres.qvs 
```

Take a peek at [corectl.yml](/examples/corectl/corectl.yml) to see how the connections are set up for corectl.
To view the tables you can then simply type:

```bash
corectl get tables
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

```bash
mvn install
java -jar ./target/core-grpc-jdbc-connector.jar
```

## Add other JDBC Drivers

Other JDBC Drivers can be added to the pom.xml file in the following section:

```xml
<dependencies>
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.2.2</version>
  </dependency>
</dependencies>
```

Make sure you start your Qlik Associative Engine with the proper gRPC connector string to enable your JDBC driver. [See an example here](./examples/docker-compose.yml).

### Athena driver

### Installing driver

The AWS Athena driver is not officially deployed on a Maven repository, so you have to download the jar file and place it in the connector project manually.

You can download the driver [here](https://docs.aws.amazon.com/athena/latest/ug/connect-with-jdbc.html).

`pom.xml` entry:

```xml
<dependency>
    <groupId>com.amazonaws.athena.jdbc</groupId>
    <artifactId>jdbcdriver</artifactId>
    <version>2.0.5</version>
</dependency>
```

Put the following lines in your `Dockerfile` before the `RUN mvn install` command:

```bash
COPY AthenaJDBC42_2.0.5.jar /usr/src/app
RUN mvn install:install-file -Dfile=/usr/src/app/AthenaJDBC42_2.0.5.jar -DgroupId=com.amazonaws.athena.jdbc -DartifactId=jdbcdriver -Dversion=2.0.5 -Dpackaging=jar
```

### Example configuration

Connection string:

```js
{
  qType: 'jdbc',
  qName: 'jdbc',
  qConnectionString: 'CUSTOM CONNECT TO "provider=jdbc;driver=awsathena;AwsRegion=eu-central-1;S3OutputLocation=s3://aws-athena-query-results-athenatest1-eu-central-1"',
  qUserName: 'AWS Key',
  qPassword: 'AWS Token',
}
```

LOAD statement:

```qlik
sql SELECT * FROM yourathendatabase.yourathenatable;
```

## License

This repository is licensed under [MIT](/LICENSE) but components used in the Dockerfile examples are under other licenses.
Make sure that you are complying with those licenses when using the built images.
