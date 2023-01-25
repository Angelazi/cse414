At first I created the table "Users". "Users" table stores the personal information such as username and password that users choose which they will need to have it ready when next time of log in, so it need to be persisted on the database so that we can check if the users input the right username and password. I also included salt and the hashed password just so that the passwords are encoded and it's safer. The balance will keep track users' balance money that they can refer to every time they log in.

I also created "Reservation" table which records all the information about the users' reserved flights which users will need to access next time when checking their flights information or to cancel them. I didn't make rid primary key because if it's indirect flight, there will be two tuples with the same fid. At the same time, I didn't create foreign keys to reference flights, I just made them normal columns in my tables.

Then the "ID" table will record the reservation id information about flights that we will refer to, it don't have to be unique but we do need to keep track of it. The "capacity" table keep tracks of how many seats left for a flight, that will be used to determine whether or not the user can still book certain flight.

These tables should be persisted on the database because these information need to be saved and stored so that users will be able to access later when needed. Information on these tables are important and unique to each user.

Itineraries can be implemented in-memory because once users log out, the information no longer exist and they can access it by search again every time they log in.
