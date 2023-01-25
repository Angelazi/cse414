SELECT DISTINCT COUNT(*) AS num_connected_cities
FROM
(SELECT DISTINCT f.origin_city, f.dest_city
FROM Flights AS f) AS f1
WHERE f1.origin_city > f1.dest_city or f1.origin_city not in
(SELECT f2.dest_city FROM Flights AS f2 WHERE f2.origin_city = f1.dest_city AND f1.origin_city = f2.dest_city AND f2.dest_city < f2.origin_city);

/*  Number of rows returned: 1.
    The query took 10s to run.
    output: 2351 */