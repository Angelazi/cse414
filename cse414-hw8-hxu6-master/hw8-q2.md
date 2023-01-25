# Q2


I used SQLite3 for this question. Assuming FLIGHTS table exists. Innermost subquery selects binned out-degree for each origin city, outer query selects out-degrees and count of cities that have such binned out-degree.

First column of csv file is minimum out-degree of this bin, second column is number of cities that fall within such bin of size 100.

### Code
```SQL
.output hw8-q2.csv
.header on
SELECT V.n AS bin_min, COUNT(*) AS num_cities
FROM
(
    SELECT (COUNT(*) / 100) * 100 AS n
    FROM FLIGHTS AS F
    GROUP BY F.origin_city
    HAVING COUNT(*) > 0
) AS V
GROUP BY V.n;
.header off
.output stdout

```