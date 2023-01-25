# Q4


I used SQLite3, Python3, and NumPy for this question. Assuming FLIGHTS table exists in hw8.db file.

The Python3 code first compute and fetch number of cities and carriers from FLIGHTS and create a ndarray X of such shape. Then it fills the X with count, assigning each city and each carrier a number representing index of X and storing them into dictionaries. After that, it computes correlation matrix and writes result to csv file.

## Code

Python3
```Python
import sqlite3
import numpy as np
from collections import OrderedDict 


conn = sqlite3.connect("hw8.db")
cursor = conn.cursor()

carrier_ids = OrderedDict()
cid_index = 0
cities = {}
c_index = 0

N = cursor.execute("SELECT COUNT(*)\
FROM (SELECT DISTINCT origin_city FROM FLIGHTS) AS T;").fetchone()[0]
C = cursor.execute("SELECT COUNT(*)\
FROM (SELECT DISTINCT carrier_id FROM FLIGHTS) AS T;").fetchone()[0]

X = np.zeros((N, C), dtype=int)
for r in cursor.execute("SELECT DISTINCT F.carrier_id, F.origin_city, COUNT(*) \
FROM FLIGHTS AS F \
GROUP BY F.carrier_id, F.origin_city;"):
    if carrier_ids.get(r[0]) == None:
        # Store indexs into dicts
        carrier_ids[r[0]] = cid_index
        cid_index += 1
    if cities.get(r[1]) == None:
        cities[r[1]] = c_index
        c_index += 1
    # Store value into data matrix
    X[cities[r[1]], carrier_ids[r[0]]] = r[2]

conn.close()
# In numpy, ndarrays of shape of (22,) and (22,1) are NOT equal
# Transpose of ndarray with shape (22,) is SAME as itself
M = np.mean(X, axis=0) # This yields shape (22,)
M = M.reshape((1, M.shape[0])) # This reshapes it into a row vector

cov = np.dot(X.transpose(), X) / N - M.transpose() * M

diag = np.diagonal(cov)
diag = diag.reshape(1, diag.shape[0]) # reshapes it into a row vector

corr = cov / np.sqrt(np.dot(diag.transpose(), diag))

with open("hw8-q4.csv", "w") as csvfile:
    csvfile.write("carrier1,carrier2,correlation\n")
    for c1, i1 in carrier_ids.items():
        for c2, i2 in carrier_ids.items():
            csvfile.write(c1 + "," + c2 + "," + "{0:.5f}".format(corr[i1, i2]) + "\n")

```