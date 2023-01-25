/*
Create a table in the database and load the data from the provided file into that table; use SQLite or any other relational DBMS if your choosing.
Turn in your create table statement. The data import statements are optional (you don't need to include these).
*/

CREATE TABLE sales(
	name VARCHAR(100),
	discount VARCHAR(30),
	month VARCHAR(10),
	price iNT
);

.mode tabs
.import  mrFrumbleData.txt sales

/* 

Find all nontrivial functional dependencies in the database.
This is a reverse engineering task, so expect to proceed in a trial and error fashion. 
Search first for the simple dependencies, say name → discount then try the more complex ones, 
like name, discount → month, as needed. To check each functional dependency you have to write a SQL query.

Your challenge is to write this SQL query for every candidate functional dependency that you check, such that:
a. the query's answer is always short (say: no more than ten lines - remember that 0 results can be instructive as well)
b. you can determine whether the FD holds or not by looking at the query's answer.
Try to be clever in order not to check too many dependencies, but don't miss potential relevant dependencies.
For example, if you have A → B and C → D, you do not need to derive AC → BD as well.
Please write a SQL query for each functional dependency you find.
Please also include a SQL query for at least one functional dependency that does not exist in the dataset.
Remember, short queries are preferred.
Don't forget to write the functional dependencies you find (and don't find!) in SQL comments.

*/

-- name -> price
SELECT *
FROM sales AS S1 , sales as S2
WHERE S1.name = S2.name AND S1.price != S2.price;

-- month -> discount
SELECT *
FROM sales AS S1 , sales as S2
WHERE S1.month = S2.month AND S1.discount != S2.discount;

-- name,discount -> price,month
-- does not hold  (not a Functional dependency)
SELECT *
FROM sales AS S1 , sales as S2
WHERE (S1.name = S2.name AND S1.discount = S2.discount) AND
	(S1.price != S2.price AND S1.month != S2.month);

-- month,price -> discount,name
-- does not hold (not a Functional dependency)
SELECT *
FROM sales AS S1 , sales as S2
WHERE S1.month = S2.month AND S1.price = S2.price AND
	S1.discount != S2.discount AND S1.name != S2.name;

/*

Functional dependencies:
name -> price
month -> discount
name,month -> price,discount (Since name -> price and month -> discount)

*/

/*

Decompose the table into Boyce-Codd Normal Form (BCNF), and create SQL tables for the decomposed schema. Create keys and foreign keys where appropriate.
For this question turn in the SQL commands for creating the tables.

*/

CREATE TABLE nameToPrice (
	name VARCHAR(100) PRIMARY KEY,
	price INT
);

CREATE TABLE monthToDiscount (
	month VARCHAR(10) PRIMARY KEY,
	discount VARCHAR(30)
);

CREATE TABLE monthToName (
	name VARCHAR(100),
	month VARCHAR(10),
	FOREIGN KEY (name) REFERENCES namePrice(name),
	FOREIGN KEY (month) REFERENCES monthDiscount(month)
);

/*
Populate your BCNF tables from Mr. Frumble's data.
For this you need to write SQL queries that load the tables you created in question 3 from the table you created in question 1.
Here, turn in the SQL queries that load the tables, 
and count the size of the tables after loading them (obtained by running SELECT COUNT(*) FROM Table).
*/

INSERT INTO nameToPrice 
SELECT DISTINCT S.name,S.price
FROM sales AS S;


SELECT COUNT(*) 
FROM nameToPrice ;-- 36 rows.  (without header)

INSERT INTO monthToDiscount 
SELECT DISTINCT S.month,S.discount
FROM sales AS S;


SELECT COUNT(*) 
FROM monthToDiscount; -- 12 rows. (withoud header)

INSERT INTO monthToName 
SELECT DISTINCT S.month,S.name
FROM sales AS S;


SELECT COUNT(*) 
FROM monthToName; -- 426 rows.  (without header)

SELECT X.name, X.price, Y.month, Y.discount
FROM nameToPrice AS X, monthToDiscount AS Y, monthToName AS Z
WHERE X.name = Z.name AND Y.month = Z.month;


