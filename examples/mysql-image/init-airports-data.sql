CREATE TABLE airports
(
  rowID integer,
  airport character varying(250),
  city character varying(250),
  country character varying(250),
  iatacode character varying(50),
  icaocode character varying(50),
  latitude character varying(50),
  longitude character varying(50),
  altitude character varying(50),
  timezone character varying(50),
  dst character varying(50),
  tz character varying(50)
);
LOAD DATA INFILE '/var/lib/mysql-files/airports.csv'
INTO TABLE airports
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS;
