# Q6


I used SQLite3 for this question.

We can see that from the 3 datasets listed, the laureate dataset is the only useful one to  compute the percentage of female laureates out of all human laureates.

So I first create a database with the Laureate.csv file, then compute the number of female laureates and total laureates from the database.

Finally compute the percentage of the female in each category.
### Code
```SQL
CREATE TABLE Laureate(id int,
first_name varchar,
surname varchar,
born varchar,
died varchar,
born_country varchar,
born_country_code varchar,
born_city varchar,
died_country varchar,
died_Country_code varchar,
died_city varchar,
gender varchar,
year int,
category varchar,
overallMotivation varchar,
share int,
motivation varchar,
name varchar,
city varchar,
country varchar
);

.mode csv
.import laureate.csv Laureate
.header on
WITH female as (SELECT COUNT(*) as number, a.category as category
FROM Laureate as a
WHERE a.gender = "female"
GROUP BY a.category),
total as (SELECT COUNT(*) as number, a.category as category
FROM Laureate as a
GROUP BY a.category)
SELECT ROUND(f.number * 100.0/t.number) as percentage, t.category as category
FROM female as f, total as t
WHERE f.category = t.category;
.header off
```