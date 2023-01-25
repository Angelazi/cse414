SELECT DISTINCT F1.origin_city AS origin_city, F1.dest_city AS dest_city, F1.actual_time AS time
FROM Flights AS F1, 
	(SELECT F.origin_city, MIN(F.actual_time) AS time
	FROM Flights AS F
	WHERE F.canceled = 0
	GROUP BY F.origin_city) AS F2
WHERE F1.origin_city = F2.origin_city AND F1.actual_time = F2.time AND F1.canceled = 0
ORDER BY time ASC, origin_city ASC;

/*  Number of rows returned: 339.
    The query took 4s to run.
    output: 
Bend/Redmond OR	Los Angeles CA	10
Burbank CA	New York NY	10
Las Vegas NV	Chicago IL	10
New York NY	Nashville TN	10
Newark NJ	Detroit MI	10
Sacramento CA	Atlanta GA	10
Washington DC	Minneapolis MN	10
Boise ID	Chicago IL	11
Boston MA	Philadelphia PA	11
Buffalo NY	Orlando FL	11
Cincinnati OH	New Haven CT	11
Denver CO	Honolulu HI	11
Denver CO	Orlando FL	11
Denver CO	Philadelphia PA	11
Fort Myers FL	Chicago IL	11
Houston TX	Salt Lake City UT	11
Minneapolis MN	Newark NJ	11
Pittsburgh PA	Dallas/Fort Worth TX	11
Indianapolis IN	Houston TX	12 */