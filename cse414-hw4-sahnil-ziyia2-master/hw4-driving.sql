/*
Translate the diagram above by writing the SQL CREATE TABLE statements to represent this E/R diagram.
Include all key constraints; you should specify both primary and foreign keys.
Make sure that your statements are syntactically correct (you might want to check them 
using SQLite, Azure SQL Server, or another relational database).
*/

CREATE TABLE person(SSN int,
                name varchar(100),
                PRIMARY KEY(SSN)
); 

CREATE TABLE Vehicle(licensePlate VARCHAR(10), 
                year int,
                insures VARCHAR(100),
                SSN INT,
                maxLiability FLOAT(24),
                PRIMARY KEY(licensePlate),
                FOREIGN KEY(SSN) REFERENCES person(SSN),
                FOREIGN KEY(insures) REFERENCES insuranceCo(name)

); 

CREATE TABLE car(licensePlate VARCHAR(10), 
                make VARCHAR(100),  
                PRIMARY KEY(licensePlate),
                FOREIGN KEY(licensePlate) REFERENCES vehicle(licensePlate)

); 

CREATE TABLE truck(licensePlate VARCHAR(10), 
                SSN INT,
                capacity int, 
                PRIMARY KEY(licensePlate),
                FOREIGN KEY(SSN) REFERENCES professionalDriver(SSN), 
                FOREIGN KEY(licensePlate) REFERENCES vehicle(licensePlate)
); 

CREATE TABLE insuranceCo(name VARCHAR(100), 
                phone int, 
                PRIMARY KEY(name)
); 


CREATE TABLE drives(licensePlate VARCHAR(25), 
                SSN int, 
                PRIMARY KEY(licensePlate,SSN),
                FOREIGN KEY(licensePlate) REFERENCES car(licensePlate),  
                FOREIGN KEY(SSN) REFERENCES NonProfessionalDriver(SSN)
);

CREATE TABLE driver(SSN int, 
                driverID int, 
                FOREIGN KEY(SSN) REFERENCES person(SSN),
                PRIMARY KEY(SSN)
); 

CREATE TABLE NonProfessionalDriver(
                SSN int,
                PRIMARY KEY(SSN),
                FOREIGN KEY(SSN) REFERENCES driver(SSN)

); 

CREATE TABLE professionalDriver(
                SSN int,
                medicalHistory TEXT,
                PRIMARY KEY(SSN),
                FOREIGN KEY(SSN) REFERENCES driver(SSN),

); 


/* 
part 2a
2. Which relation in your relational schema represents the 
   relationship "insures" in the E/R diagram? Why is that your representation?

ANS: The insures relation is a many to one relation. A vehicle can only be insured by 
     one company but one company can insure many vehicles. It is included in the vehicle table 
     as an attribute as an foreign key which references name of the insurance company.
     This attribute can only be null if the vehicle does not have any insurance.
*/

/*
part 2a
3. Compare the representation of the relationships "drives" and "operates" in your schema,
   and explain why they are different.

ANS: Drives is a table and a many-many realtion as car can be driven by many non professional drivers
     and non professional drivers can drive many cars while operates is an attribute which is many-one 
     as proffesional drivers can drive many trucks but one truck can onle be driven by one
     professional driver.



*/