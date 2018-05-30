FROM openjdk:8-jdk

# ----
# Install Maven
RUN apt-get install curl tar bash
ARG MAVEN_VERSION=3.5.3
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

## speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# ----
# Install PostgreSql JDBC Drivers

# ----
# Install project dependencies and keep sources
# make source folder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# install maven dependency packages (keep in image)
COPY pom.xml /usr/src/app

RUN mvn install # && rm -rf target
COPY src /usr/src/app/src

ENTRYPOINT  ["mvn", "exec:java", "-e", "-Dexec.mainClass=qlik.jdbc.connector.GrpcServer"]

