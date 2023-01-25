-- hw1-q5.sql

SELECT M.ID, M.Minute 
FROM MyRestaurants AS M 
WHERE M.Minute <= 20 
ORDER BY M.ID ASC;