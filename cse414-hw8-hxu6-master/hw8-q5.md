# Q5


I used SQLite3 and Python3 for this question. Assuming FLIGHTS table exists in hw8.db file.

The code finds the answer by Breadth-First Searching a directed graph whose vertices are cities and directed edges are flights. At the beginning it fetches all city names from SQLite to create CityNodes and store them into citygraph, a dict.

Then, it begins to construct edges of citygraph: Flights. It fetches minimal flight distance for each origin_city and dest_city pair to create Flights that connect CityNodes together.

After that, it begins to BFS the citygraph. The path to vertices and mileage will be kept track of. It will stop searching if the mileage of the path is bigger than known minimum distance traveled from origin to destination.

## Output
```
Itinerary 1: Seattle WA -> Salt Lake City UT -> Twin Falls ID
Itinerary 2: Seattle WA -> Boise ID -> Salt Lake City UT -> Twin Falls ID
Greta Thunberg now will only travel for 864 miles.
It takes 2.63726 seconds to do BFS.
```

## Code

Python3
```Python
import sqlite3
import queue
import time

conn = sqlite3.connect("hw8.db")
cursor = conn.cursor()

class CityNode:
    def __init__(self, name):
        self.name = name
        self.out_flights = []

class Flight:
    def __init__(self, dest, distance):
        self.dest = dest
        self.distance = distance

citygraph = {}

for r in cursor.execute("""
SELECT city FROM ( 
SELECT DISTINCT origin_city AS city
FROM FLIGHTS
UNION
SELECT DISTINCT dest_city AS city
FROM FLIGHTS)
ORDER BY city;"""):
    citygraph[r[0]] = CityNode(r[0])


for r in cursor.execute("""SELECT F.origin_city AS origin_city, 
F.dest_city AS dest_city, MIN(F.distance) AS mindis 
FROM FLIGHTS AS F 
GROUP BY F.origin_city, F.dest_city;"""):
    f = Flight(citygraph[r[1]], r[2])
    citygraph[r[0]].out_flights.append(f)

conn.close()

shortestroutes = []
min_distance = 0

def BFS(origin, dest):
    global shortestroutes
    global min_distance
    q = queue.Queue()
    q.put(([origin], 0))
    while not q.empty():
        path = q.get()
        c = path[0][-1]
        if c == dest:
            if (path[1] == min_distance):
                shortestroutes.append(path[0])
            if (path[1] < min_distance or len(shortestroutes) == 0):
                shortestroutes.clear()
                shortestroutes.append(path[0])
                min_distance = path[1]
        for f in c.out_flights:
            if path[1] + f.distance < min_distance or len(shortestroutes) == 0:
                new_path = list(path[0])
                new_path.append(f.dest)
                q.put((new_path, path[1] + f.distance))

starttime = time.time()
BFS(citygraph["Seattle WA"], citygraph["Twin Falls ID"])
endtime = time.time()


for i in range(len(shortestroutes)):
    print("Itinerary " + str(i + 1) + ": ", end="")
    for j in range(len(shortestroutes[i])):
        print(shortestroutes[i][j].name, end=(" -> " if j < len(shortestroutes[i]) - 1 else "\n"))
print("Greta Thunberg now will only travel for " + str(min_distance) + " miles.")
print("It takes " + "{0:.5f}".format(endtime - starttime) + " seconds to do BFS.")
```