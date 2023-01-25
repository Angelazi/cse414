SELECT DISTINCT C.name AS carrier
FROM Flights F, Carriers C 
WHERE F.carrier_id = C.cid
AND F.origin_city = 'Seattle WA'
AND F.dest_city = 'San Francisco CA'
ORDER BY C.name ASC;

/*  Number of rows returned: 4.
    The query took 4s to run.
    output: 
Alaska Airlines Inc.
SkyWest Airlines Inc.
United Air Lines Inc.
Virgin America */