package flightapp;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.transform.Result;
/**
 * Runs queries against a back-end database
 */
public class Query {
  private String username_current;
  private Map<Integer, List<Itinerary>> itinerarymap = new HashMap<>();
  private boolean logIn;

  // DB Connection
  private Connection conn;

  // Password hashing parameter constants
  private static final int HASH_STRENGTH = 65536;
  private static final int KEY_LENGTH = 128;

  // Canned queries
  private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement checkFlightCapacityStatement;

  // For check dangling
  private static final String TRANCOUNT_SQL = "SELECT @@TRANCOUNT AS tran_count";
  private PreparedStatement tranCountStatement;

  private static final String CLEAR_TABLES = "DELETE FROM users; DELETE FROM capacity; DELETE FROM reservations;" +
                                             "DELETE FROM ID";
  private PreparedStatement clearTableStatement;

  private static final String CLEAR_ITINERARY = "DELETE FROM itinerary";
  private PreparedStatement clearItineraryStatement;

  private static final String CHECK_USERNAME = "SELECT COUNT(*) FROM users A WHERE A.username = ?";
  private PreparedStatement checkUsernameStatement; // 1 if username exists, 0 if not

  private static final String CREATE__USER_ACCOUNT = "INSERT INTO users VALUES (?, ?, ?, ?)";
  private PreparedStatement createUserAccountStatement;

  private static final String GET_SALT = "SELECT A.salt FROM users A WHERE A.username = ?";
  private PreparedStatement getSaltStatement;

  private static final String GET_HASHED_PASSWORD = "SELECT A.hashed_password FROM users A WHERE A.username = ?";
  private PreparedStatement getHashPasswordStatement;

  private static final String DIRECT_FLIGHTS = "SELECT TOP (?) F.fid AS fid, F.day_of_month AS day_of_month, " +
        "F.carrier_id AS carrier_id, F.flight_num AS flight_num, F.origin_city AS origin_city, " +
        "F.dest_city AS dest_city, F.actual_time AS actual_time, F.capacity AS capacity, F.price AS price " +
        "FROM Flights F " +
        "WHERE F.origin_city = ? AND F.dest_city = ? AND F.day_of_month = ? " +
        "AND F.canceled = 0" +
        "ORDER BY F.actual_time, F.fid"; //default ASC
  private PreparedStatement directFlightsStatement;

  private static final String INDIRECT_FLIGHTS = "SELECT TOP (?) F1.flight_num AS f1_flight_num, " +
  "F1.origin_city AS f1_origin_city, F1.dest_city AS f1_dest_city, F1.actual_time AS f1_actual_time, " +
  "F1.carrier_id AS f1_carrier_id, F1.day_of_month AS f1_day_of_month, F1.fid AS f1_fid, " +
  "F1.capacity AS f1_capacity, F1.price AS f1_price, F2.flight_num AS f2_flight_num, F2.origin_city AS f2_origin_city, " +
  "F2.dest_city AS f2_dest_city, F2.actual_time AS f2_actual_time, F2.carrier_id AS f2_carrier_id, " +
  "F2.day_of_month AS f2_day_of_month, F2.fid AS f2_fid, F2.capacity AS f2_capacity, F2.price AS f2_price, " +
  "(F1.actual_time + F2.actual_time) AS total_time " +
  "FROM Flights AS F1 JOIN Flights AS F2 on F1.dest_city = F2.origin_city AND F1.day_of_month = F2.day_of_month " +
  "WHERE F1.day_of_month = ? AND F1.origin_city = ? AND F2.dest_city = ? AND " +
  "F1.canceled = 0 AND F2.canceled = 0 " +
  "ORDER BY F1.actual_time + F2.actual_time, F1.fid, F2.fid ";
  private PreparedStatement indirectFlightsStatement;

  private static final String COUNT_RESERVATIONS = "SELECT COUNT(*) FROM reservations R WHERE R.day_of_month = ? AND " +
                                                          "R.username = ?";
  private PreparedStatement countReservationsStatement;

  private static final String NUM_CAPACITY = "SELECT COUNT(*) FROM capacity C WHERE C.fid = ?";
  private PreparedStatement numCapacityStatement;

  private static final String INSERT_CAPACITY = "INSERT INTO capacity VALUES (?, 1)";
  private PreparedStatement insertCapacityStatement;

  private static final String GET_NUM_BOOKINGS = "SELECT C.num_bookings FROM capacity C WHERE C.fid = ?";
  private PreparedStatement getNumBookingsStatement;

  private static final String INCREASE_BOOKINGS = "UPDATE capacity SET num_bookings = num_bookings + 1 WHERE fid = ?";
  private PreparedStatement increaseBookings;

  private static final String COUNT_ID = "SELECT COUNT(*) FROM ID I";
  private PreparedStatement countIDStatement;

  private static final String INSERT_ID = "INSERT INTO ID VALUES (1)";
  private PreparedStatement insertIDStatement;

  private static final String INCREASE_ID = "UPDATE ID SET id = id + 1";
  private PreparedStatement increaseIDStatement;

  private static final String GET_ID = "SELECT I.id from ID I";
  private PreparedStatement getIDStatement;

  private static final String ADD_RESERVATION = "INSERT INTO reservations VALUES " +
          "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private PreparedStatement addReservationStatement;

  private static final String CHECK_RESERVATION = "SELECT COUNT(*) AS cnt, R.username FROM reservations R " +
                                                  "WHERE R.rid = ? AND R.paid = 0 AND R.cancelled = 0" +
                                                  "GROUP BY R.username HAVING R.username = ?";
  private PreparedStatement checkReservationStatement;

  private static final String GET_RES_PRICE = "SELECT R.price FROM reservations R WHERE R.rid = ?";
  private PreparedStatement getResPriceStatement;

  private static final String GET_BAL = "SELECT A.balance FROM users A WHERE A.username = ?";
  private PreparedStatement getBalStatement;

  private static final String PAY_UPDATE = "UPDATE reservations SET paid = 1 WHERE rid = ?";
  private PreparedStatement payReservationStatement;

  private static final String UPDATE_BALANCE = "UPDATE users SET balance = balance + ? WHERE username = ?";
  private PreparedStatement updateBalanceStatement;

  private static final String GET_RESERVATIONS = "SELECT R.rid AS rid, R.fid AS fid, R.day_of_month AS day_of_month, " +
  "R.carrier_id AS carrier_id, R.flight_num AS flight_num, R.origin AS origin_city, " +
  "R.dest AS dest_city, R.duration AS actual_time, R.capacity AS capacity, " +
  "R.price AS price, R.paid AS paid, R.direct AS direct " +
  "FROM reservations R " +
  "WHERE R.cancelled = 0 AND R.username = ?";
  private PreparedStatement getReservationStatement;

  private static final String CHECK_OTHER_RESERVATION = "SELECT COUNT(*) FROM reservations R WHERE R.username = ?";
  private PreparedStatement checkOtherReservationStatement;

  //for this one, we already checked that given rid belongs to current username in canceled()
  private static final String CANCEL_RESERVATION = "UPDATE reservations SET cancelled = 1 WHERE rid = ?";
  private PreparedStatement cancelReservationStatement;

  public Query() throws SQLException, IOException {
    this(null, null, null, null);
    this.logIn = false;
  }

  protected Query(String serverURL, String dbName, String adminName, String password)
          throws SQLException, IOException {
    conn = serverURL == null ? openConnectionFromDbConn()
            : openConnectionFromCredential(serverURL, dbName, adminName, password);

    prepareStatements();
  }

  /**
   * Return a connecion by using dbconn.properties file
   *
   * @throws SQLException
   * @throws IOException
   */
  public static Connection openConnectionFromDbConn() throws SQLException, IOException {
    // Connect to the database with the provided connection configuration
    Properties configProps = new Properties();
    configProps.load(new FileInputStream("dbconn.properties"));
    String serverURL = configProps.getProperty("hw5.server_url");
    String dbName = configProps.getProperty("hw5.database_name");
    String adminName = configProps.getProperty("hw5.username");
    String password = configProps.getProperty("hw5.password");
    return openConnectionFromCredential(serverURL, dbName, adminName, password);
  }

  /**
   * Return a connecion by using the provided parameter.
   *
   * @param serverURL example: example.database.widows.net
   * @param dbName    database name
   * @param adminName username to login server
   * @param password  password to login server
   *
   * @throws SQLException
   */
  protected static Connection openConnectionFromCredential(String serverURL, String dbName,
                                                           String adminName, String password) throws SQLException {
    String connectionUrl =
            String.format("jdbc:sqlserver://%s:1433;databaseName=%s;user=%s;password=%s", serverURL,
                    dbName, adminName, password);
    Connection conn = DriverManager.getConnection(connectionUrl);

    // By default, automatically commit after each statement
    conn.setAutoCommit(true);

    // By default, set the transaction isolation level to serializable
    conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

    return conn;
  }

  /**
   * Get underlying connection
   */
  public Connection getConnection() {
    return conn;
  }

  /**
   * Closes the application-to-database connection
   */
  public void closeConnection() throws SQLException {
    conn.close();
  }

  /**
   * Clear the data in any custom tables created.
   *
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      clearTableStatement.executeUpdate();
      clearTableStatement.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method.
   */
  private void prepareStatements() throws SQLException {
    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);
    tranCountStatement = conn.prepareStatement(TRANCOUNT_SQL);
    clearTableStatement = conn.prepareStatement(CLEAR_TABLES);
    clearItineraryStatement = conn.prepareStatement(CLEAR_ITINERARY);
    checkUsernameStatement = conn.prepareStatement(CHECK_USERNAME);
    createUserAccountStatement = conn.prepareStatement(CREATE__USER_ACCOUNT);
    getSaltStatement = conn.prepareStatement(GET_SALT);
    getHashPasswordStatement = conn.prepareStatement(GET_HASHED_PASSWORD);
    directFlightsStatement = conn.prepareStatement(DIRECT_FLIGHTS,
            ResultSet.TYPE_SCROLL_INSENSITIVE,  ResultSet.CONCUR_READ_ONLY);
    indirectFlightsStatement = conn.prepareStatement(INDIRECT_FLIGHTS,
            ResultSet.TYPE_SCROLL_INSENSITIVE,  ResultSet.CONCUR_READ_ONLY);
    countReservationsStatement = conn.prepareStatement(COUNT_RESERVATIONS);
    insertCapacityStatement = conn.prepareStatement(INSERT_CAPACITY);
    getNumBookingsStatement = conn.prepareStatement(GET_NUM_BOOKINGS);
    increaseBookings = conn.prepareStatement(INCREASE_BOOKINGS);
    increaseIDStatement = conn.prepareStatement(INCREASE_ID);
    countIDStatement = conn.prepareStatement(COUNT_ID);
    insertIDStatement = conn.prepareStatement(INSERT_ID);
    getIDStatement = conn.prepareStatement(GET_ID);
    addReservationStatement = conn.prepareStatement(ADD_RESERVATION);
    numCapacityStatement = conn.prepareStatement(NUM_CAPACITY);
    checkReservationStatement = conn.prepareStatement(CHECK_RESERVATION);
    getResPriceStatement = conn.prepareStatement(GET_RES_PRICE);
    getBalStatement = conn.prepareStatement(GET_BAL);
    payReservationStatement = conn.prepareStatement(PAY_UPDATE);
    updateBalanceStatement = conn.prepareStatement(UPDATE_BALANCE);
    getReservationStatement = conn.prepareStatement(GET_RESERVATIONS,
            ResultSet.TYPE_SCROLL_INSENSITIVE,  ResultSet.CONCUR_READ_ONLY);
    checkOtherReservationStatement = conn.prepareStatement(CHECK_OTHER_RESERVATION);
    cancelReservationStatement = conn.prepareStatement(CANCEL_RESERVATION);
  }

  // input password, salt, returns salted password's hash
  private byte[] generateHash(String password, byte[] salt) {
    // Specify the hash parameters
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, HASH_STRENGTH, KEY_LENGTH);

    // Generate the hash
    SecretKeyFactory factory = null;
    byte[] hash = null;
    try {
      factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      return factory.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IllegalStateException();
    }
  }

  private byte[] generateSalt() {
    // Generate a random cryptographic salt
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return salt;
  }

  /**
   * Takes a user's username and password and attempts to log the user in.
   *
   * @param username user's username
   * @param password user's password
   *
   * @return If someone has already logged in, then return "User already logged in\n" For all other
   *         errors, return "Login failed\n". Otherwise, return "Logged in as [username]\n".
   */
  public String transaction_login(String username, String password) {
    try {
      if (logIn) {
        return "User already logged in\n";
      } else {
        checkUsernameStatement.setString(1, username);
        ResultSet rs1 = checkUsernameStatement.executeQuery();
        rs1.next();
        if (rs1.getInt(1) == 1) { //username is valid, exists in accounts table
          getSaltStatement.setString(1, username); //get username's salt
          ResultSet rs2 = getSaltStatement.executeQuery();
          rs2.next();
          byte[] salt = rs2.getBytes(1);
          byte[] attemptedPasswordHash = generateHash(password, salt); // entered password's hash

          getHashPasswordStatement.setString(1, username); //get correct password's hash
          ResultSet rs3 = getHashPasswordStatement.executeQuery();
          rs3.next();

          if (Arrays.equals(attemptedPasswordHash, rs3.getBytes(1))) { //correct password
            this.logIn = true;
            rs1.close(); rs2.close(); rs3.close();
            this.username_current = username;//to user in inserting reservation
            return String.format("Logged in as %s\n", username);
          } else {
            rs1.close(); rs2.close(); rs3.close();
            return "Login failed\n";
          }
        }
        rs1.close();
        return "Login failed\n"; //not logged in & username invalid
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      checkDanglingTransaction();
    }
    return "Login failed\n";
  }

  /**
   * Implement the create user function.
   *
   * @param username   new user's username. User names are unique the system.
   * @param password   new user's password.
   * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure
   *                   otherwise).
   *
   * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
   */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    for (int i = 0; i < 3; i++) {
      try {
        conn.setAutoCommit(false);
        checkUsernameStatement.setString(1, username);
        ResultSet resultset = checkUsernameStatement.executeQuery();
        resultset.next();
        if (initAmount >= 0 && resultset.getInt(1) == 0) { 
          byte[] salt = generateSalt();
          byte[] hashed_password = generateHash(password, salt);
          createUserAccountStatement.setString(1, username);
          createUserAccountStatement.setBytes(2, hashed_password);
          createUserAccountStatement.setBytes(3, salt);
          createUserAccountStatement.setInt(4, initAmount);
          createUserAccountStatement.executeUpdate();
          //createUserAccountStatement.close();
          resultset.close();
          conn.commit();
          conn.setAutoCommit(true);
          return String.format("Created user %s\n", username);
        } else {
          resultset.close();
          conn.setAutoCommit(true);
          return "Failed to create user\n";
        }
      } catch (SQLException e) {
        try {
          if (isDeadLock(e)) {
            conn.rollback();
          }
          conn.setAutoCommit(true);
        } catch (SQLException se) {
          e.printStackTrace();
        }
      } finally {
        checkDanglingTransaction();
      }
    }
    return "Failed to create user\n";
  }

  /**
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination city, on the given day
   * of the month. If {@code directFlight} is true, it only searches for direct flights, otherwise
   * is searches for direct flights and flights with two "hops." Only searches for up to the number
   * of itineraries given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight        if true, then only search for direct flights, otherwise include
   *                            indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   *
   * @return If no itineraries were found, return "No flights match your selection\n". If an error
   *         occurs, then return "Failed to search\n".
   *
   *         Otherwise, the sorted itineraries printed in the following format:
   *
   *         Itinerary [itinerary number]: [number of flights] flight(s), [total flight time]
   *         minutes\n [first flight in itinerary]\n ... [last flight in itinerary]\n
   *
   *         Each flight should be printed using the same format as in the {@code Flight} class.
   *         Itinerary numbers in each search should always start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */
  public String transaction_search(String originCity, String destinationCity, boolean directFlight,
                                   int dayOfMonth, int numberOfItineraries) {
    try {
      // WARNING the below code is unsafe and only handles searches for direct flights
      // You can use the below code as a starting reference point or you can get rid
      // of it all and replace it with your own implementation.

      StringBuffer sb = new StringBuffer();
      try {
        itinerarymap = new HashMap<>();// first clear itinerary from last search
        //regardless of directFlights only or not, we always get direct flight itineraries
        directFlightsStatement.setInt(1, numberOfItineraries);
        directFlightsStatement.setString(2, originCity);
        directFlightsStatement.setString(3, destinationCity);
        directFlightsStatement.setInt(4, dayOfMonth);
        ResultSet directRS = directFlightsStatement.executeQuery();

        int counter = 0;
        if (directFlight || (!directFlight && getResultSetSize(directRS) == numberOfItineraries)) { // direct only, one call
          while (directRS.next()) {
            addDirectFlightsToBuffer(sb, directRS, counter);
            counter++;
          }
        } else { //need to sort both indirect and direct based on time
          indirectFlightsStatement.setInt(1, numberOfItineraries);
          indirectFlightsStatement.setInt(2, dayOfMonth);
          indirectFlightsStatement.setString(3, originCity);
          indirectFlightsStatement.setString(4, destinationCity);
          ResultSet indirectRS = indirectFlightsStatement.executeQuery();

          int maxIndirect = numberOfItineraries - getResultSetSize(directRS);
          int indirectCount = 0;
          while (counter != numberOfItineraries && (directRS.next() || indirectRS.next())) {
            //undo next()
            directRS.previous();
            indirectRS.previous();
            if (indirectCount != maxIndirect && tieBreaker(directRS, indirectRS) > 0) {
              addIndirectFlightsToBuffer(sb, indirectRS, counter);// add indirect to buffer
              indirectRS.next(); //advance indirect only forward once
              indirectCount++; //increment number of indirect flights added
              counter++; //Increment itinerary ID
            } else { //direct flight is faster
              addDirectFlightsToBuffer(sb, directRS, counter);//add direct flight to buffer
              directRS.next();//advance direct only forward once
              counter++; //increment ItineraryID
            }
          }
          indirectRS.close();
        }
        directRS.close();
        //if no itineraries found
        if (counter == 0) {
          return "No flights match your selection\n";
        } else {
          return sb.toString();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return sb.toString();
    } finally {
      checkDanglingTransaction();
    }
  }

  // add direct flight itinerary to given string buffer
  private void addDirectFlightsToBuffer(StringBuffer sb, ResultSet resultset, int itineraryID) {
    try {
      int flightID = resultset.getInt("fid");
      int dayOfMonth = resultset.getInt("day_of_month");
      String carrierID = resultset.getString("carrier_id");
      int flightNum = resultset.getInt("flight_num");
      String originCity = resultset.getString("origin_city");
      String destCity = resultset.getString("dest_city");
      int time = resultset.getInt("actual_time");
      int capacity = resultset.getInt("capacity");
      int price = resultset.getInt("price");

      Flight f = new Flight(flightID, dayOfMonth, carrierID, flightNum, originCity,
              destCity, time, capacity, price);
      sb.append("Itinerary " + itineraryID + ": 1 flight(s), " + time + " minutes\n");
      sb.append(f.toString() + "\n");
      //add to itinerary table
      //iid, fid, day, cid, flightNum, origin, dest, duration, capacity, price
      List<Itinerary> list = new ArrayList<>();
      list.add(new Itinerary(flightID, dayOfMonth, carrierID, flightNum, originCity, destCity,
              time, capacity, price));
      itinerarymap.put(itineraryID, list);
      } catch (SQLException e) {
        e.printStackTrace();
      }
  }

  //add indirect flight itinerary to given string buffer
  private void addIndirectFlightsToBuffer(StringBuffer sb, ResultSet resultset, int itineraryID) {
    try {
        int f1_flightID = resultset.getInt("f1_fid");
        int f1_dayOfMonth = resultset.getInt("f1_day_of_month");
        String f1_carrierID = resultset.getString("f1_carrier_id");
        int f1_flightNum = resultset.getInt("f1_flight_num");
        String f1_originCity = resultset.getString("f1_origin_city");
        String f1_destCity = resultset.getString("f1_dest_city");
        int f1_time = resultset.getInt("f1_actual_time");
        int f1_capacity = resultset.getInt("f1_capacity");
        int f1_price = resultset.getInt("f1_price");

        int f2_flightID = resultset.getInt("f2_fid");
        int f2_dayOfMonth = resultset.getInt("f2_day_of_month");
        String f2_carrierID = resultset.getString("f2_carrier_id");
        int f2_flightNum = resultset.getInt("f2_flight_num");
        String f2_originCity = resultset.getString("f2_origin_city");
        String f2_destCity = resultset.getString("f2_dest_city");
        int f2_time = resultset.getInt("f2_actual_time");
        int f2_capacity = resultset.getInt("f2_capacity");
        int f2_price = resultset.getInt("f2_price");
        int total_time = resultset.getInt("total_time");

        Flight f1 = new Flight(f1_flightID, f1_dayOfMonth, f1_carrierID, f1_flightNum, f1_originCity,
                               f1_destCity, f1_time, f1_capacity, f1_price);
        Flight f2 = new Flight(f2_flightID, f2_dayOfMonth, f2_carrierID, f2_flightNum, f2_originCity,
                               f2_destCity, f2_time, f2_capacity, f2_price);
        sb.append("Itinerary " + itineraryID + ": 2 flight(s), " + total_time + " minutes\n");
        sb.append(f1.toString() + "\n");
        sb.append(f2.toString() + "\n");
        //add to itinerary table
        Itinerary it1 = new Itinerary(f1_flightID, f1_dayOfMonth, f1_carrierID, f1_flightNum, f1_originCity,
                f1_destCity, f1_time, f1_capacity, f1_price);
        Itinerary it2 = new Itinerary(f2_flightID, f2_dayOfMonth, f2_carrierID, f2_flightNum, f2_originCity,
                f2_destCity, f2_time, f2_capacity, f2_price);
        List<Itinerary> list = new ArrayList<>();
        list.add(it1);
        list.add(it2);
        itinerarymap.put(itineraryID, list);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Implements the book itinerary function.
   *
   * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in
   *                    the current session.
   *
   * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
   *         If the user is trying to book an itinerary with an invalid ID or without having done a
   *         search, then return "No such itinerary {@code itineraryId}\n". If the user already has
   *         a reservation on the same day as the one that they are trying to book now, then return
   *         "You cannot book two flights in the same day\n". For all other errors, return "Booking
   *         failed\n".
   *
   *         And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n"
   *         where reservationId is a unique number in the reservation system that starts from 1 and
   *         increments by 1 each time a successful reservation is made by any user in the system.
   */
  public String transaction_book(int itineraryId) {
    for (int j = 0; j < 3; j++) {
      try {
        if (!logIn) {
          return "Cannot book reservations, not logged in\n";
        } else if (itinerarymap.isEmpty() || !itinerarymap.containsKey(itineraryId)) {
          return String.format("No such itinerary %d\n", itineraryId);
        } else if (alreadyBookedOnDay(itineraryId)) {
          return "You cannot book two flights in the same day\n";
        }
        //book itinerary
        conn.setAutoCommit(false);
        List<Itinerary> ourItinList = itinerarymap.get(itineraryId);
        int direct = ourItinList.size() % 2;
        //check capacity for all flights in ourItinList
        for (Itinerary i : ourItinList) {
          int capacity = i.capacity;
          int fid = i.fid;
          int num_bookings = getNbooked(fid);
          if (capacity == num_bookings) {
            conn.setAutoCommit(true);
            return "Booking failed\n";
          }
        }
        //if code reaches here, it means booking is possible
        for (Itinerary i : ourItinList) {
          int fid = i.fid;
          if (isInCapacityTable(fid)) {
            //increment booking numbers
            increaseBookings.setInt(1, fid);
            increaseBookings.executeUpdate();
          } else {
            insertCapacityStatement.setInt(1, fid);
            insertCapacityStatement.executeUpdate();
          }
        }
        //update ID table
        if (isIDEmpty()) {
          insertIDStatement.executeUpdate();
        } else {
          increaseIDStatement.executeUpdate();
        }
        //get next ID
        ResultSet rs2 = getIDStatement.executeQuery();
        rs2.next();
        int id = rs2.getInt(1);
        //insert into reservations BOTH FLIGHTS IF NECESSARY (rid=ID, username, fid, day, cid, #, orig, dest, time, cap, price);
        for (Itinerary i : ourItinList) {
          insertIntoReservations(i, id, this.username_current, direct);
        }

        conn.commit();
        conn.setAutoCommit(true);
        return String.format("Booked flight(s), reservation ID: %d\n", id);
      } catch (SQLException e) {
        e.printStackTrace();
        try {
          if (isDeadLock(e)) {
            conn.rollback();
          }
          conn.setAutoCommit(true);
        } catch (SQLException se) {
          se.printStackTrace();
        }
      } finally {
        checkDanglingTransaction();
      }
    }
    return "Booking failed\n";
  }

  //insert itinerary into reservations given username, itineraryID and reservation id
  private void insertIntoReservations(Itinerary i, int rid, String username, int direct) {
    try {
      //fid, day, cid, flightNum, origin, dest, duration, capacity, price
      int fid = i.fid;
      int day = i.day;
      String cid = i.carrier_ID;
      int flightNum = i.flightNum;
      String origin = i.origin;
      String dest = i.dest;
      int duration = i.duration;
      int capacity = i.capacity;
      int price = i.price;

      conn.setAutoCommit(false);
      //rid, username, fid, day, cid, flightNum, origin, dest, duration, capacity, price
      addReservationStatement.setInt(1, rid);
      addReservationStatement.setString(2, username);
      addReservationStatement.setInt(3, fid);
      addReservationStatement.setInt(4, day);
      addReservationStatement.setString(5, cid);
      addReservationStatement.setInt(6, flightNum);
      addReservationStatement.setString(7, origin);
      addReservationStatement.setString(8, dest);
      addReservationStatement.setInt(9, duration);
      addReservationStatement.setInt(10, capacity);
      addReservationStatement.setInt(11, price);
      addReservationStatement.setInt(12, 0); //not paid
      addReservationStatement.setInt(13, 0); //not cancelled
      addReservationStatement.setInt(14, direct); // 1 if direct
      addReservationStatement.executeUpdate();

      conn.commit();
      conn.setAutoCommit(true);
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        if (isDeadLock(e)) {
          conn.rollback();
        }
        conn.setAutoCommit(true);
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }

  //return true if given fid's flightis already in capacity
  private boolean isInCapacityTable(int fid) {
    try {
      numCapacityStatement.setInt(1, fid);
      ResultSet resultset = numCapacityStatement.executeQuery();
      resultset.next();
      return resultset.getInt(1) == 1;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  //return nbooked from capacity table given itinerary's fid
  private int getNbooked(int itineraryFID) {
    try {
      getNumBookingsStatement.setInt(1, itineraryFID);
      ResultSet resultset = getNumBookingsStatement.executeQuery();
      if (!resultset.next()) { //flight is not entered in yet to reservations table
        return 0;
      } else {
        return resultset.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  // returns true if person already has reservation that flies on same day as that of given itineraryId's flight
  private boolean alreadyBookedOnDay(int itineraryId) {
    try {
      int dayOfItinerary = itinerarymap.get(itineraryId).get(0).day;
      countReservationsStatement.setInt(1, dayOfItinerary);
      countReservationsStatement.setString(2, this.username_current);
      ResultSet rs2 = countReservationsStatement.executeQuery();
      rs2.next();
      return rs2.getInt(1) != 0; // return true if count of booked flights not zero
    } catch (SQLException e) {
      e.printStackTrace();
      return true;
    }
  }

  //return true if ID table is empty
  private boolean isIDEmpty() {
    try {
      ResultSet resultset = countIDStatement.executeQuery();
      resultset.next();
      return resultset.getInt(1) == 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return true;
    }
  }

  /**
   * Implements the pay function.
   *
   * @param reservationId the reservation to pay for.
   *
   * @return If no user has logged in, then return "Cannot pay, not logged in\n" If the reservation
   *         is not found / not under the logged in user's name, then return "Cannot find unpaid
   *         reservation [reservationId] under user: [username]\n" If the user does not have enough
   *         money in their account, then return "User has only [balance] in account but itinerary
   *         costs [cost]\n" For all other errors, return "Failed to pay for reservation
   *         [reservationId]\n"
   *
   *         If successful, return "Paid reservation: [reservationId] remaining balance:
   *         [balance]\n" where [balance] is the remaining balance in the user's account.
   */
  public String transaction_pay(int reservationId) {
    for (int i = 0; i < 3; i++) {
      try {
        if (!logIn) {
          return "Cannot pay, not logged in\n";
        }
        int balance = getUserBal(this.username_current);
        int cost = getPrice(reservationId);
        if (!checkReservation(reservationId, this.username_current)) {
          return String.format("Cannot find unpaid reservation %d under user: %s\n", reservationId, this.username_current);
        } else if (balance < cost) {
          return String.format("User has only %d in account but itinerary costs %d\n", balance, cost);
        }
        conn.setAutoCommit(false);
        //mark reservations as paid
        payReservationStatement.setInt(1, reservationId);
        payReservationStatement.executeUpdate();

        // reduce account balance by cost
        updateBalanceStatement.setInt(1, -1*cost); //add -1*cost to balance
        updateBalanceStatement.setString(2, this.username_current);
        updateBalanceStatement.executeUpdate();
        conn.commit();
        conn.setAutoCommit(true);
        return  String.format("Paid reservation: %d remaining balance: %d\n", reservationId, balance-cost);
      } catch (SQLException e) {
        try {
          e.printStackTrace();;
          if (isDeadLock(e)) {
            conn.rollback();
          }
          conn.setAutoCommit(true);
        } catch (SQLException se) {
          return String.format("Failed to pay for reservation %d\n", reservationId);
        }
      } finally {
        checkDanglingTransaction();
      }
    }
    return String.format("Failed to pay for reservation %d\n", reservationId);
  }

  //returns true if given username and rid, there is a valid reservation
  private boolean checkReservation(int reservationId, String username) {
    try {
      checkReservationStatement.setInt(1, reservationId);
      checkReservationStatement.setString(2, username);
      ResultSet resultset = checkReservationStatement.executeQuery();
      return resultset.next(); // false if empty result ie no valid reservation
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  //get user balance given username
  private int getUserBal(String username) {
    try {
      getBalStatement.setString(1, username);
      ResultSet rs2 = getBalStatement.executeQuery();
      rs2.next();
      int balance = rs2.getInt(1);
      return balance;
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  // get cost of reservation given rid
  //while loop because if indirect, returns 2 flights
  private int getPrice(int rid) {
    try {
      getResPriceStatement.setInt(1, rid);
      ResultSet rs1 = getResPriceStatement.executeQuery();
      int cost = 0;
      while (rs1.next()) {
        cost += rs1.getInt(1);
      }
      return cost;
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * Implements the reservations function.
   *
   * @return If no user has logged in, then return "Cannot view reservations, not logged in\n" If
   *         the user has no reservations, then return "No reservations found\n" For all other
   *         errors, return "Failed to retrieve reservations\n"
   *
   *         Otherwise return the reservations in the following format:
   *
   *         Reservation [reservation ID] paid: [true or false]:\n [flight 1 under the
   *         reservation]\n [flight 2 under the reservation]\n Reservation [reservation ID] paid:
   *         [true or false]:\n [flight 1 under the reservation]\n [flight 2 under the
   *         reservation]\n ...
   *
   *         Each flight should be printed using the same format as in the {@code Flight} class.
   *
   * @see Flight#toString()
   */
  public String transaction_reservations() {
    for (int i = 0; i < 3; i++){
      try {
        if (!logIn) {
          return "Cannot view reservations, not logged in\n";
        } else if (!checkOtherReservation(this.username_current)) {
          return "No reservations found\n";
        }

        conn.setAutoCommit(false);
        StringBuffer sb = new StringBuffer();
        getReservationStatement.setString(1, this.username_current);
        ResultSet resultset = getReservationStatement.executeQuery();
        while (resultset.next()) {
          int direct = resultset.getInt("direct");
          int paid = resultset.getInt("paid");
          int rid = resultset.getInt("rid");
          if (direct == 1) { //direct reservation
            sb.append("Reservation " + rid + " paid: " + (paid == 1) + ":\n");
            addReservationToBuffer(sb, resultset);
          } else { //indirect reservation, add next two flights to stringbuffer
            sb.append("Reservation " + rid + " paid: " + (paid == 1) + ":\n");
            addReservationToBuffer(sb, resultset); //add first indirect flight
            resultset.next();
            addReservationToBuffer(sb, resultset); //add second indirect flight
          }
        }

        conn.commit();
        conn.setAutoCommit(true);
        return sb.toString();
      } catch (SQLException e) {
        e.printStackTrace();
        try {
          if (isDeadLock(e)) {
            conn.rollback();
          }
          conn.setAutoCommit(true);
        } catch (SQLException se) {
          return "Failed to retrieve reservations\n";
        }
      } finally {
        checkDanglingTransaction();
      }
    }
    return "Failed to retrieve reservations\n";
  }

  //REDUNDANT, can edit method used in search, need to clean up search()
  private void addReservationToBuffer(StringBuffer sb, ResultSet resultset) {
    try {
      int flightID = resultset.getInt("fid");
      int dayOfMonth = resultset.getInt("day_of_month");
      String carrierID = resultset.getString("carrier_id");
      int flightNum = resultset.getInt("flight_num");
      String originCity = resultset.getString("origin_city");
      String destCity = resultset.getString("dest_city");
      int time = resultset.getInt("actual_time");
      int capacity = resultset.getInt("capacity");
      int price = resultset.getInt("price");
      Flight f = new Flight(flightID, dayOfMonth, carrierID, flightNum, originCity,
              destCity, time, capacity, price);
      sb.append(f.toString() + "\n");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  //returns true if given username has any reservation
  private boolean checkOtherReservation(String username) {
    try {
      checkOtherReservationStatement.setString(1, username);
      ResultSet resultset = checkOtherReservationStatement.executeQuery();
      resultset.next();
      return resultset.getInt(1) != 0; //if count is 0, has no reservations
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Implements the cancel operation.
   *
   * @param reservationId the reservation ID to cancel
   *
   * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n" For
   *         all other errors, return "Failed to cancel reservation [reservationId]\n"
   *
   *         If successful, return "Canceled reservation [reservationId]\n"
   *
   *         Even though a reservation has been canceled, its ID should not be reused by the system.
   */
  public String transaction_cancel(int reservationId) {
    for (int i = 0; i < 3; i++) {
      try {
          if (!logIn) {
            return "Cannot cancel reservations, not logged in\n";
          }
          if (checkReservation(reservationId, this.username_current)) { //true if rid belongs to username's account
            conn.setAutoCommit(false);
            int cost = getPrice(reservationId);
            //Refund
            updateBalanceStatement.setInt(1, cost);
            updateBalanceStatement.setString(2, this.username_current);
            updateBalanceStatement.executeUpdate();
            // mark reservations as cancelled
            cancelReservationStatement.setInt(1, reservationId);
            cancelReservationStatement.executeUpdate();
            conn.commit();
            conn.setAutoCommit(true);
            return String.format("Canceled reservation %d\n", reservationId);
          }
      } catch (SQLException e) {
        e.printStackTrace();
        try {
          if (isDeadLock(e)) {
            conn.rollback();
          }
          conn.setAutoCommit(true);
        } catch(SQLException se) {
          se.printStackTrace();
        }
        return "Failed to cancel reservation " + reservationId + "\n";
      } finally {
        checkDanglingTransaction();
      }
    }
    return "Failed to cancel reservation " + reservationId + "\n";
  }

  /**
   * Example utility function that uses prepared statements
   */
  private int checkFlightCapacity(int fid) throws SQLException {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }

  /**
   * Throw IllegalStateException if transaction not completely complete, rollback.
   *
   */
  private void checkDanglingTransaction() {
    try {
      try (ResultSet resultset = tranCountStatement.executeQuery()) {
        resultset.next();
        int count = resultset.getInt("tran_count");
        if (count > 0) {
          throw new IllegalStateException(
                  "Transaction not fully commit/rollback. Number of transaction in process: " + count);
        }
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Database error", e);
    }
  }

  private static boolean isDeadLock(SQLException ex) {
    return ex.getErrorCode() == 1205;
  }

  //Helper function to compare order between direct and indirect flight
  //Returns negative integer if direct flight is quicker, positive integer if indirect flight is quicker
  private int tieBreaker(ResultSet directRS, ResultSet indirectRS) {
    try {
      if (!directRS.next()) { //if directRS is finished
        return 1;
      } else if (!indirectRS.next()) { //indirectRS is finished
        return -1;
      } else {
        int directTime = directRS.getInt("actual_time");
        int indirectTime = indirectRS.getInt("total_time");
        if (directTime != indirectTime) {
          return directTime - indirectTime;
        } else { //break tie by fid
          int directFID = directRS.getInt("fid");
          int indirectFID1 = indirectRS.getInt("f1_fid");
          int indirectFID2 = indirectRS.getInt("f2_fid");
          if (directFID != indirectFID1) {
            return directFID - indirectFID1;
          } else {
            return directFID - indirectFID2;
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  //Helper function to return size of a ResultSet
  private int getResultSetSize(ResultSet resultset) {
    try {
      int size = 0;
      if (resultset != null) {
        resultset.beforeFirst();
        resultset.last();
        size = resultset.getRow();
        resultset.beforeFirst();
      }
      return size;
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  //Class to store info about a single itinerary result from search
  class Itinerary {
    //, fid, day, cid, flightNum, origin, dest, duration, capacity, price
    public int fid;
    public int day;
    public String carrier_ID;
    public int flightNum;
    public String origin;
    public String dest;
    public int duration;
    public int capacity;
    public int price;

    public Itinerary(int fid, int day, String cid, int flightNum, String origin, String dest,
                     int duration, int capacity, int price) {
      this.fid = fid;
      this.day = day;
      this.carrier_ID = cid;
      this.flightNum = flightNum;
      this.origin = origin;
      this.dest = dest;
      this.duration = duration;
      this.capacity = capacity;
      this.price = price;
    }
  }

  /**
   * A class to store flight information.
   */
  class Flight {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public int flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    public Flight(int fid, int day, String carrierID, int flightNum, String origin, String dest,
                  int time, int capacity, int price) {
      this.fid = fid;
      this.dayOfMonth = day;
      this.carrierId = carrierID;
      this.flightNum = flightNum;
      this.originCity = origin;
      this.destCity = dest;
      this.time = time;
      this.capacity = capacity;
      this.price = price;
    }

    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: "
              + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time
              + " Capacity: " + capacity + " Price: " + price;
    }
  }
}