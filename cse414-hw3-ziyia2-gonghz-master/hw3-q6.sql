SELECT DISTINCT F.origin_city AS city
FROM Flights AS F
WHERE F.origin_city NOT IN (
    SELECT F2.dest_city
	FROM Flights AS F1 JOIN Flights AS F2 ON F1.dest_city = F2.origin_city
	WHERE F1.origin_city = 'Seattle WA'
	)
ORDER BY F.origin_city ASC;

/*  Number of rows returned: 4.
    The query took 4s to run.
    output:
Devils Lake ND
Hattiesburg/Laurel MS
St. Augustine FL
Victoria TX */