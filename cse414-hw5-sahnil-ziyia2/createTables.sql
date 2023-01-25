

-- CREATE MASTER KEY ENCRYPTION BY PASSWORD = 'CsE344RaNdOm$Key';

-- CREATE DATABASE SCOPED
-- CREDENTIAL QueryCredential
-- WITH IDENTITY = 'reader2020', SECRET = '20wiUWcse()';

-- CREATE EXTERNAL DATA SOURCE CSE344_EXTERNAL
-- WITH
-- ( TYPE = RDBMS,
--   LOCATION='khangishandsome.database.windows.net',
--   DATABASE_NAME = 'cse344_readonly',
--   CREDENTIAL = QueryCredential
-- );


-- DROP EXTERNAL TABLE  Flights;
-- DROP EXTERNAL TABLE  Carriers;
-- DROP EXTERNAL TABLE  Weekdays;
-- DROP EXTERNAL TABLE  Months;
-- DROP  TABLE  users;
-- DROP  TABLE  reservations;
-- DROP  TABLE  ID;
-- DROP  TABLE  capacity;

-- CREATE EXTERNAL TABLE Flights(
--   fid int,
--   month_id int,
--   day_of_month int,
--   day_of_week_id int,
--   carrier_id varchar(7),
--   flight_num int,
--   origin_city varchar(34),
--   origin_state varchar(47),
--   dest_city varchar(34),
--   dest_state varchar(46),
--   departure_delay int,
--   taxi_out int,
--   arrival_delay int,
--   canceled int,
--   actual_time int,
--   distance int,
--   capacity int,
--   price int
-- ) WITH (DATA_SOURCE = CSE344_EXTERNAL);

-- CREATE EXTERNAL TABLE Carriers(
--   cid varchar(7),
--   name varchar(83)
-- ) WITH (DATA_SOURCE = CSE344_EXTERNAL);

-- CREATE EXTERNAL TABLE Weekdays(
--   did int,
--   day_of_week varchar(9)
-- ) WITH (DATA_SOURCE = CSE344_EXTERNAL);

-- CREATE EXTERNAL TABLE Months
-- (
--   mid int,
--   month varchar(9)
-- ) WITH (DATA_SOURCE = CSE344_EXTERNAL);


-- SELECT COUNT(*) FROM Flights;  -- expect count of 1148675



CREATE TABLE users (
    username varchar(20) PRIMARY KEY,
    hashed_password VARBINARY(20),
    salt VARBINARY(16),
    balance INT, 
);

CREATE TABLE reservations (
    rid INT, --not primary key because if indirect flight, we add two tuples with same rid
    username varchar(20),
    fid INT, 
    day_of_month INT,
    carrier_id varchar(7),
    flight_num INT,
    origin varchar(34),
    dest varchar(34),
    duration INT,
    capacity INT,
    price INT, 
    paid INT, -- 0 if not paid, 1 if paid
    cancelled INT, -- 1 if cancelled, 0 o/w
    direct INT -- 1 if direct, 0 if indirect
);

CREATE TABLE ID (
    id INT
);

CREATE TABLE capacity (
    fid int,
    num_bookings int
);
    