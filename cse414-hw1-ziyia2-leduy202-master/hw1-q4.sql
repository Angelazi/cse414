-- hw1-q4.sql

.headers ON
SELECT * FROM MyRestaurants;

.mode list
.separator ", "
SELECT * FROM MyRestaurants; 

.mode col 
.width 15
SELECT * FROM MyRestaurants;