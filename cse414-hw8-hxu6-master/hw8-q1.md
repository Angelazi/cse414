# Q1


I used SQLite3 for this question. I first imported flights csv files into indexed FLIGHTS table and created a table INFLUXS to store names of cities and their influxs. I then update INFLUXS for each rows in INFLUXS and output them into csv file.

Name of city with the least influx is in the first line of csv file.

Name of city with the greatest influx is in the second line of csv file.

### Code
```SQL
CREATE TABLE FLIGHTS (fid int PRIMARY KEY, 
         month_id int,        -- 1-12
         day_of_month int,    -- 1-31 
         day_of_week_id int,  -- 1-7, 1 = Monday, 2 = Tuesday, etc
         carrier_id varchar(7),
         flight_num int,
         origin_city varchar(34), 
         origin_state varchar(47), 
         dest_city varchar(34), 
         dest_state varchar(46), 
         departure_delay int, -- in mins
         taxi_out int,        -- in mins
         arrival_delay int,   -- in mins
         canceled int,        -- 1 means canceled
         actual_time int,     -- in mins
         distance int,        -- in miles
         capacity int, 
         price int,             -- in $ 
         FOREIGN KEY(month_id) REFERENCES MONTHS(mid),
         FOREIGN KEY(day_of_week_id) REFERENCES WEEKDAYS(did),
         FOREIGN KEY(carrier_id) REFERENCES CARRIERS(cid)            
         );

CREATE INDEX F_origin_city ON FLIGHTS(origin_city);
CREATE INDEX F_dest_city ON FLIGHTS(dest_city);


.mode csv
.import "flights-small.csv" FLIGHTS

CREATE TABLE INFLUXS(
    city varchar(34) PRIMARY KEY,
    influx int
);

INSERT INTO INFLUXS (city, influx)
SELECT DISTINCT origin_city, 0
FROM FLIGHTS
UNION
SELECT DISTINCT dest_city, 0
FROM FLIGHTS;

UPDATE INFLUXS
SET influx = influx + (
    SELECT COUNT(*)
    FROM FLIGHTS AS F
    WHERE INFLUXS.city = F.dest_city
);

UPDATE INFLUXS
SET influx = influx - (
    SELECT COUNT(*)
    FROM FLIGHTS AS F
    WHERE INFLUXS.city = F.origin_city
);


.output hw8-q1.csv
.header on
SELECT city, influx
FROM INFLUXS AS I
WHERE I.influx = (SELECT MIN(influx) FROM INFLUXS);
.header off
SELECT city, influx
FROM INFLUXS AS I
WHERE I.influx = (SELECT MAX(influx) FROM INFLUXS);

.output stdout

```