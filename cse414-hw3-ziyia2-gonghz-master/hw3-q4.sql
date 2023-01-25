SELECT DISTINCT F.origin_city AS origin_city, CASE WHEN F2.C is NULL then 0
else (F2.C * 1.0 / COUNT(F.fid) * 100) end as percentage
FROM Flights AS F LEFT OUTER JOIN (
    SELECT F2.origin_city, COUNT(*) AS C
    FROM Flights AS F2
    WHERE F2.actual_time < 3 * 60
    AND F2.canceled = 0
    GROUP BY F2.origin_city
    ) AS F2 ON F.origin_city = F2.origin_city
GROUP BY F.origin_city, F2.C
ORDER BY percentage ASC;

/*  Number of rows returned: 327.
    The query took 7s to run.
    output:
origin_city	percentage
Guam TT	null
Pago Pago TT	null
Aguadilla PR	28.679245283000
Anchorage AK	31.656277827200
San Juan PR	33.543916853400
Charlotte Amalie VI	39.270072992700
Ponce PR	40.322580645100
Fairbanks AK	49.539170506900
Kahului HI	53.341183397100
Honolulu HI	54.533695511500
San Francisco CA	55.223708487000
Los Angeles CA	55.412788344700
Seattle WA	57.410932825600
New York NY	60.532437322300
Long Beach CA	61.719979024600
Kona HI	62.952799121800
Newark NJ	63.367565254500
Plattsburgh NY	64.000000000000
Las Vegas NV	64.471006179900 */