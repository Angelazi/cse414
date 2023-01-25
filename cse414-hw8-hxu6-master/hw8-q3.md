# Q3


I used SQLite3 and Python3 for this question. Assuming FLIGHTS table exists in hw8.db file.

The code first fetch origin cities from FLIGHTS to create cities and citydict dict that stores cities and their out-degree.

After that it makes another query to fetch all distinct origin_city dest_city pairs from FLIGHTS, which represents connections between cities, to compute adjacency matrix and out-degree vector. Here out-degrees of a city means number of connections from this city to other cities.

It then computes PageRank using given algorithm, sort them, write result to csv file, and print number of iterations it takes.
It takes 7 iterations to converge.

## Code

Python3
```Python
import sqlite3
import numpy as np

conn = sqlite3.connect("hw8.db")
cursor = conn.cursor()

cities = []
citydict = {}
assigned_index = 0

for r in cursor.execute("""SELECT DISTINCT origin_city AS city
    FROM FLIGHTS
    UNION
    SELECT DISTINCT dest_city AS city
    FROM FLIGHTS;"""):
    cities.append([r[0], 0, assigned_index])
    citydict[r[0]] = cities[assigned_index]
    assigned_index += 1

v = np.full(len(cities), 1.0 / len(cities), dtype=float)
lastv = v

a = np.zeros((len(cities), len(cities)), dtype=int)

for r in cursor.execute("""SELECT DISTINCT origin_city, dest_city
                        FROM FLIGHTS AS F;"""):
    a[citydict[r[0]][2], citydict[r[1]][2]] = 1
    citydict[r[0]][1] += 1

conn.close()

def aaa():
    t = np.full(len(cities), 0.1 / len(cities), dtype=float)
    for i in range(len(cities)):
        for j in range(len(cities)):
            t[i] += 0.9 * a[i, j] * lastv[j] / cities[j][1]
    return t

v = aaa()
it = 1

while abs(sum(v - lastv)) > 0.0001:
    lastv = v
    v = aaa()
    it += 1

print(it)

out = []
for i in range(len(cities)):
    out.append((cities[i][0], v[i]))
out = sorted(out, key=lambda x: x[1], reverse=True)

with open("hw8-q3.csv", "w") as csvfile:
    csvfile.write("city,pagerank\n")
    for i in range(len(cities)):
        csvfile.write(out[i][0] + "," + "{0:.5f}".format(out[i][1]) + "\n")

```