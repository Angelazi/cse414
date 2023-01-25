1. 
CREATE TABLE Edges(Source INT, Destination INT);
INSERT INTO Edges VALUES (8,5),(6,22),(1,3),(5,5);

INSERT INTO Edges VALUES ('-1','2000'); 
-- doesn't give an error since it was predetermined type INT so 
-- what you trying to insert program will attempt to parse into 
-- that type

SELECT * FROM Edges;

SELECT Source FROM Edges;

SELECT * FROM Edges 
WHERE Source > Destination;