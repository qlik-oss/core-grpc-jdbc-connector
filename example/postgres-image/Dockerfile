FROM postgres:10.0
COPY ./airports.csv /
COPY ./init-airports-data.sql /docker-entrypoint-initdb.d/
