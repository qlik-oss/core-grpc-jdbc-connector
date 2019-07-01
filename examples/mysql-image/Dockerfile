FROM mysql:8.0.0 /testing
COPY ./airports.csv /var/lib/mysql-files/
COPY ./init-airports-data.sql /docker-entrypoint-initdb.d/
