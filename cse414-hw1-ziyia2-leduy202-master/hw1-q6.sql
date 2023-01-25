-- hw1-q6.sql

SELECT M.ID, M.Visited 
FROM MyRestaurants AS M 
WHERE M.Veggie == 1 AND M.Visited < DATE ('now', '-3 months');