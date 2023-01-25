SELECT DISTINCT F2.dest_city AS city
FROM Flights AS F2
WHERE F2.origin_city != 'Seattle WA'
AND F2.dest_city != 'Seattle WA'
AND F2.dest_city NOT IN (
    SELECT F.dest_city
    FROM FLIGHTS AS F
    WHERE F.origin_city = 'Seattle WA'
    )
AND F2.origin_city IN (
    SELECT F.dest_city
    FROM Flights AS F
    WHERE F.origin_city = 'Seattle WA'
    )
ORDER BY city ASC;

/*  Number of rows returned: 256.
    The query took 3s to run.
    output:
Aberdeen SD
Abilene TX
Adak Island AK
Aguadilla PR
Akron OH
Albany GA
Albany NY
Alexandria LA
Allentown/Bethlehem/Easton PA
Alpena MI
Amarillo TX
Appleton WI
Arcata/Eureka CA
Asheville NC
Ashland WV
Aspen CO
Atlantic City NJ
Augusta GA
Bakersfield CA */