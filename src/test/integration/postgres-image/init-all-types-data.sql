CREATE TABLE all_types
(
  type_small_int smallint,
  type_integer integer,
  type_bigint bigint,
  type_bit bit,
  type_numeric numeric(6,4),
  type_double double precision,
  type_real real,
  type_decimal decimal,
  type_boolean boolean,
  type_date date,
  type_time time,
  type_timestamp timestamp,
  type_text text,
  type_varchar varchar(20),
  type_char char
);
COPY all_types FROM '/all_types.csv' DELIMITER ',' CSV HEADER;