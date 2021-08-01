package com.example.ReportsDemo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.postgresql.util.PGobject;

public class DBUtility {

    public static String url = makeURL(
            "postgresql_postGIS",  // postGIS necessary for interacting with geom columns
            "localhost",
            "brianstrock",
            5432
    );

    // these are readability variables used to set cutoffs for enumeration lengths and other magic numbers/sentinels

    public static final int USER_CUTOFF = 8; // attributes 1-8 are user table attributes
    public static final int REPORT_CUTOFF = 13; // 9-13 are report attributes
    public static final int CONTACT_CUTOFF = 14;
    public static final int HAS_EMERGENCY_CONTACT = 4;

    public static Connection connect(String url, HttpServletRequest request) throws SQLException, IOException, ClassNotFoundException {
        // this function gets you a url to connect to
        try {
            Class.forName("org.postgis.DriverWrapper");  // load postgis
            ServletContext context = request.getServletContext();  // we use this for logging
            InputStream in = context.getResourceAsStream("/WEB-INF/db.properties");  // load database password, etc.

            final Properties connProps = new Properties();  // initialize object for user/pass
            connProps.load(in);  // load user/pass from properties input stream
            Connection conn = DriverManager.getConnection(url, connProps);
            ((org.postgresql.PGConnection) conn).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
            return conn;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    protected static void printReports(ResultSet res) throws SQLException {
        // prints and formats the results in a ResultSet
        if (res != null) {  // if we have reports
            while (res.next()) {  // until we run out of reports

                // print some info...
                int id = res.getInt("id");
                String reportType = res.getString("report_type");
                String disasterType = res.getString("disaster_type");
                String timestamp = res.getTimestamp("timestamp").toString();
                String geom = res.getString("geom");

                String header = "**************************\n";
                String body = String.format("ID: %d\n" +
                        "REPORT TYPE: %s\n" +
                        "DISASTER TYPE: %s]\n" +
                        "TIMESTAMP: %s GMT\n" +
                        "LOCATION: %s\n", id, reportType, disasterType, timestamp, geom);
                System.out.println(header + body + header + "\n");
            }
        }
    }

    public static ArrayList<LinkedHashMap<String, String>> requestParamsToArrayList(HttpServletRequest request) {
        // this function sorts request parameters into an arraylist of LinkedHashMaps

        ServletContext context = request.getServletContext();
        Enumeration<String> keys = request.getParameterNames();  // name of our parameters will be keys in the...

        // LINKEDHASHMAPS FOR ATTRIBUTE STORAGE
        LinkedHashMap<String, String> userTable = new LinkedHashMap<>();  // where we store the user table key-values
        LinkedHashMap<String, String> reportTable = new LinkedHashMap<>();  // where we store the report table key-values
        LinkedHashMap<String, String> subReportTable = new LinkedHashMap<>();  // where we store the sub-report key-values
        LinkedHashMap<String, String> emergencyContact = new LinkedHashMap<>();

        // make a list for the hashmaps
        ArrayList<LinkedHashMap<String, String>> insertAttributes = new ArrayList<>();  // THIS IS RETURNED

        // loop through keys, get attribute values, use cutoffs to place them in the correct LinkedHashMap
        int i = 0;  // starting counter value

        while (keys.hasMoreElements()) {  // as long as we have a key to work on
            String key = keys.nextElement();  // move cursor and get the key
            if (i != 0) {  // first parameter is tab_id, which we skip -- i gets incremented after if block closure
                String val = request.getParameter(key);  // get value for the key

                if (i < USER_CUTOFF) {  // goes in the user table map
                    userTable.put(key, val);
                } else if (i < REPORT_CUTOFF) { // only triggers if i > user_cutoff
                    reportTable.put(key, val);
                } else if (i < CONTACT_CUTOFF) {
                    subReportTable.put(key, val); // only triggers if i < contact_cutoff
                } else {
                    emergencyContact.put(key.replace("c_", ""), val); // this must be emergency contact

                }
            }
            i++; // increments i = 0 in order to skip tab_id
        }

        // pack the sorted LinkedHashMaps into the container and return
        insertAttributes.add(userTable);
        insertAttributes.add(reportTable);
        insertAttributes.add(subReportTable);

        // the length of insertAttributes informs a sentinel value later, so we only add the final hashmap if emergency contact is set
        if (i > CONTACT_CUTOFF) {
            insertAttributes.add(emergencyContact);
        }


        return insertAttributes;
    }

    public static PreparedStatement insertParams(LinkedHashMap<String, String> tableAttributes, PreparedStatement raw, int locationMode) throws SQLException {
        // this function packs the supplied key-value pairs into the supplied prepared statement
        // the statement can then be executed

        int counter = 1;

        // handles creating point objects for statements with geometry datatypes
        if (locationMode == 1) {
            // set up a coords object and populate with lon/lat values
            String[] coords = new String[2];
            coords[0] = tableAttributes.get("longitude");
            coords[1] = tableAttributes.get("latitude");

            // first we need a string representation of the geometry
            String geomString = String.format("POINT %s %s", coords[0], coords[1]);
            Geometry geom = PGgeometry.geomFromString(geomString);  // then we objectify it
            raw.setObject(counter, new PGgeometry(geom));  // remember when we added the geometry type to the connection?  good times.

            // these need to be removed in order for everything else to work
            tableAttributes.remove("latitude");
            tableAttributes.remove("longitude");
            counter++;  // moving on
        }

        // we're going to set string and integer values into our prepared statement
        for (String key : tableAttributes.keySet()) {
            String fieldInput = tableAttributes.get(key);  // get parameter value
            try {  // can it be integerized?  set it as an integer...
                int isInt = Integer.parseInt((String.valueOf(fieldInput)));
                raw.setInt(counter, isInt);
            } catch (NumberFormatException nfe) {  // oh, that's not an integer?
                raw.setString(counter, fieldInput);  // set it as a string.
            }  // it we only have strings and integers at this point!
            counter++;  // moving along
        }
        // now all of our statements have been parameterized!
        return raw;
    }

    public static void createReport(HttpServletRequest request) throws SQLException, IOException, ClassNotFoundException {
        ServletContext context = request.getServletContext();  // use this for logging
        int userID = 0, reportID = 0, contactID = 0; // initialize ID containers

        context.log("INITIATING DB CONNECTION");
        Connection conn = DBUtility.connect(DBUtility.url, request);
        context.log("DB CONNECTION SUCCESSFUL");
        //((org.postgresql.PGConnection) conn).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
        context.log("UNPACKING PARAMETERS");
        ArrayList<LinkedHashMap<String, String>> params = requestParamsToArrayList(request);  // the parameters are accessed from here
        context.log("PARAMETERS UNPACK SUCCESSFUL");

        context.log("PREPARING STATEMENTS");
        // we're going to set up our prepared statements and initialize baseline values to check the database for existing user
        // statements will be added to this list for future operations
        ArrayList<PreparedStatement> statements = new ArrayList<>();

        String email = params.get(0).get("email");  // primary key for user table- presence checked against this, etc.

        // INITIALIZE QUERIES //
        PreparedStatement getUserID = conn.prepareStatement(Statements.get("fetchUser"));
        PreparedStatement getReportID = conn.prepareStatement(Statements.get("fetchReport"));  // prepare get report ID
        getUserID.setString(1, email);  // insert email into userID query

        // INITIALIZE INSERTS //
        PreparedStatement rawUser = conn.prepareStatement(Statements.get("makeUser"));
        PreparedStatement rawReport = conn.prepareStatement(Statements.get("makeReport"));

        context.log("PREPARED STATEMENTS CREATED");
        context.log("ADDING PARAMETERS TO STATEMENTS");

        // remember that cool function that takes a linked hashmap of database table key-value pairs and smashes them
        // into our SQL statements?  good times!  secure times!  <^_^>
        PreparedStatement userSQL = insertParams(params.get(0), rawUser, 0);
        context.log("PARAMETERS ADDED TO INSERT USER STATEMENT");
        PreparedStatement reportSQL = insertParams(params.get(1), rawReport, 1);  // has a geometry object
        context.log("PARAMETERS ADDED TO INSERT REPORT STATEMENT");

        // EMERGENCY CONTACT HANDLING
        // we check if the data package contains an emergency contact id
        // we also check if the contact user already exist in the database, so that we don't violate unique constraints

        if (params.size() == HAS_EMERGENCY_CONTACT) {  // this protocol engaged when emergency contact is specified
            context.log("EMERGENCY CONTACT DETECTED");
            String contactEmail = params.get(3).get("email");  // the emergency contact email
            PreparedStatement getContactID = conn.prepareStatement(Statements.get("fetchUser")); // prepare statement
            getContactID.setString(1, contactEmail);  // insert email in query string
            context.log(getContactID.toString());
            statements.add(getContactID);  // we'll use this list later
            try {
                ResultSet res = getContactID.executeQuery();  // check for emergency contact in db
                if (res.next()) {  // if there's a result
                    context.log("EMERGENCY CONTACT LOCATED IN DB");
                    contactID = res.getInt("id");  // get the ID and save
                    res.close();
                } else {  // if this is a new contact
                    context.log("CREATING EMERGENCY CONTACT");

                    PreparedStatement makeContact = conn.prepareStatement(Statements.get("makeContact"));  // prepare statement
                    PreparedStatement contactSQL = insertParams(params.get(3), makeContact, 0);  // insert values
                    contactSQL.executeUpdate();  // execute emergency contact creation
                    context.log("EMERGENCY CONTACT CREATED SUCCESSFULLY");
                    res = getContactID.executeQuery();  // retrieve contact ID we just created
                    if (res.next()) {  // move cursor
                        contactID = res.getInt("id"); // set contactID
                        res.close();
                    }
                }
            } catch (SQLException e) {
                context.log("AN ERROR OCCURRED WHILE HANDLING EMERGENCY CONTACT PROTOCOL");
                e.printStackTrace();
                throw e;
            }
        }

        context.log("PREPARING EXECUTION");

        // adding statements to our list
        statements.add(userSQL);
        statements.add(getUserID);
        statements.add(reportSQL);
        statements.add(getReportID);

        // USER CREATION HANDLING
        // we're going to check if we need to create a user, or use an existing one.
        // then we'll create the user (if needed), or capture userID if not.

        try {
            ResultSet res = getUserID.executeQuery();  // check if user exists

            if (res.next()) {  // if the user does exist
                context.log("EXISTING USER LOCATED");
                userID = res.getInt("id");  // set the userID
                res.close();
            } else {  // if the user doesn't exist
                context.log("CREATING NEW USER");
                if (contactID != 0) {  // if we found an emergency contact previously
                    userSQL.setInt(8, contactID);  // set the emergency contactID for this user
                } else {  // if we didn't find a contact ID
                    userSQL.setNull(8, Types.NULL); // set it to NULL
                }
                userSQL.executeUpdate(); // execute create user statement
                res = getUserID.executeQuery();  // get the user ID we just created
                if (res.next()) {  // move cursor
                    userID = res.getInt("id");  // set userID
                    res.close();
                    context.log("NEW USER CREATED SUCCESSFULLY");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        // REPORT CREATION HANDLING
        // we're going to use the user's inputs to create the report
        // once we create the report, we'll get its ID and use that to populate the reportType table,
        // along with its secondary value (resource_type, disaster_type, etc. etc.) using linked table inheritance

        try {
            context.log("CREATING REPORT");
            reportSQL.setInt(5, userID);  // set the userID in the report
            reportSQL.executeUpdate();  // execute create report statement

            getReportID.setInt(1, userID);  // we need to specify user ID in order to get the right reportID
            ResultSet res = getReportID.executeQuery();  // get the reportID we just created

            if (res.next()) {  // set cursor
                reportID = res.getInt("id");  // set the reportID
                res.close();
                context.log("REPORT CREATED SUCCESSFULLY");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        try {
            context.log("CREATING SUB-REPORT");
            String type = params.get(1).get("report_type");  // get the report type
            context.log("type " + type);
            String reportTypeTableName = type.toLowerCase() + "_report";  // set the DB tablename for this report type

            String typeColumn;  // we'll use this later

            // request report types have a different column name value, so we substitute here
            if (type.equals("REQUEST") || type.equals("DONATION")) {
                typeColumn = "resource_type";
            } else {  // otherwise, type + _type = name of column
                typeColumn = type.toLowerCase() + "_type";
                context.log("type " + typeColumn);
            }

            // we're going to initialize another SQL statement, but there's a problem...
            // PreparedStatements wrap inserted values with '', which causes accessors (table name, column name) to fail
            // thus, we use string.format to insert tablenames
            // (security-concerned? these values are not supplied by the user- no risk of injection)

            context.log("ADDING PARAMETERS TO SUB-REPORT");
            // rawSQL is populated with tablename and type column name
            String rawReportType = String.format(Statements.get("makeReportType"), // this is the raw SQL
                    reportTypeTableName, // this is the tabletype parameter
                    typeColumn); // this is the sub-report column name
            context.log(typeColumn);
            String typeValue = params.get(2).get(typeColumn); // get the type for this report
            context.log(params.get(2).keySet().toString());
            context.log(params.get(2).values().toString());
            context.log("typeValue " + typeValue);
            PreparedStatement reportTypeSQL = conn.prepareStatement(rawReportType);  // prepare report type statement
            reportTypeSQL.setInt(1, reportID);  // insert report ID value
            reportTypeSQL.setString(2, typeValue);  // insert type column value
            context.log("PARAMETERS ADDED TO SUB-REPORT");
            reportTypeSQL.executeUpdate();  // execute the statement
            statements.add(reportTypeSQL);  // add it to the statements list
            context.log("SUB-REPORT CREATED SUCCESSFULLY");

            context.log("EXECUTION SUCCESSFUL");  // boom

        } catch (SQLException e) {
            context.log("STATEMENT EXECUTION FAILURE: CHECK SERVER LOGS FOR REFERENCE POINT");  //  aahhhhh!
            context.log(e.toString());
            e.printStackTrace();
            throw e;
        } finally {
            context.log("CLOSING STATEMENTS");
            for (PreparedStatement s : statements) {  // remember our statements list?  good times.
                s.close(); // let's close all the statements!
            }
            context.log("ALL STATEMENTS CLOSED SUCCESSFULLY");
            context.log("CLOSING CONNECTION");
            conn.close();  // close the connection regardless of success or the db will cry
            context.log("CONNECTION CLOSED SUCCESSFULLY");
        }
    }

    public static String makeURL(String driver,
                                 String host,
                                 String db,
                                 int port) {

        // helper function to create database url strings...
        // the "stringtype=unspecified" query parameter is completely necessary
        return String.format("jdbc:%s://%s:%d/%s?stringtype=unspecified", driver, host, port, db);
    }

    public static void queryReport(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ClassNotFoundException, JSONException {
        // configure connection and unpack parameters
        ServletContext context = request.getServletContext();
        Connection conn = DBUtility.connect(DBUtility.url, request);
        ArrayList<LinkedHashMap<String, String>> params = requestParamsToArrayList(request);  // the parameters are accessed from here
        LinkedHashMap<String, String> queryParams = params.get(0);  // we just need the one
        String reportTypeTableName = null, reportType = null;

        int counter = 0;

        // we're going to loop through the query parameter keys to get the linked tablename and associated column name
        for (String key : queryParams.keySet()) {
            switch (counter) {
                case 0:
                    counter++;
                    break;
                case 1:
                    reportTypeTableName = queryParams.get(key).toLowerCase() + "_report";
                    counter++;
                    break;
                case 2:
                    reportType = key;
            }
        }
        JSONObject returnParams = new JSONObject();
        // get the query SQL and prepare statement
        String raw = String.format(Statements.get("queryReports"), reportTypeTableName, reportType);
        PreparedStatement queryPrepared = conn.prepareStatement(raw);
        // insert parameters into query statement
        PreparedStatement querySQL = insertParams(queryParams, queryPrepared, 0);
        // run statement
        ResultSet res = querySQL.executeQuery();
        if (res.next()) { // move cursor to first result
            counter = 1;
            while (res.next()) {  // for each result
                ResultSetMetaData meta = res.getMetaData();  // highly useful
                LinkedHashMap<String, String> row = new LinkedHashMap<>();  // stores values for this result row
                for (int i = 1; i < meta.getColumnCount(); i++) {  // loop through columns

                    String col = meta.getColumnName(i);  // get column name
                    String val;
                    if (meta.getColumnTypeName(i).equals("geometry")) {  // handling geometry column
                        PGgeometry geom = (PGgeometry) res.getObject(col);  // it's a geometry object
                        val = geom.toString();
                    } else {
                        val = res.getString(col);  // everything else is a string
                    }
                    row.put(col, val);  // add to the row
                    System.out.println(col.toUpperCase() + ": " + val);
                }
                returnParams.put(String.valueOf(counter), row);  // row captured- add to json
                counter++;  // next one!
            }
        }
        // add to JSON, set headers and fire away
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(returnParams);
        out.flush();  // bye bye json!
    }

    public static void queryAllReports(HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException, SQLException, IOException {

        ServletContext context = request.getServletContext();
        context.log("PRINTING REPORTS");

        // initialize connection and add geometry type
        context.log("INITIATING DB CONNECTION");
        Connection conn = DBUtility.connect(DBUtility.url, request);
        context.log("DB CONNECTION SUCCESSFUL");
        //((org.postgresql.PGConnection) conn).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));

        // prepare query statement
        PreparedStatement getAllReports = conn.prepareStatement(Statements.get("getAllReports"));
        try {
            ResultSet res = getAllReports.executeQuery();  // execute statement
            if (res.next()) { // move cursor to first result
                String headerFooter = "************** REPORT %s **************";  // formatted later
                int ALL_INTEGERS_REPORTED = 3;  // our megaquery returns the ID field a lot, we'll filter extraneous results out
                int id_count = 0;  // above is the threshold, this is the counter
                while (res.next()) {  // for each result
                    ResultSetMetaData meta = res.getMetaData();  // tells us some things that are good to know
                    int size = meta.getColumnCount();  // like how many columns there are
                    System.out.println(String.format(headerFooter, "START"));  // print header

                    for (int i = 1; i < size; i++) {  // for each column
                        String col = meta.getColumnName(i);  // get name
                        String type = meta.getColumnTypeName(i);  // data type will assist in string formatting...
                        // we need to handle each data type to string conversion depending on its type
                        // that typename thing sure comes in handy
                        switch (type) {
                            // all integers handled in this case clause
                            case "bigserial":
                            case "int8":
                            case "int4":
                                if (id_count <= ALL_INTEGERS_REPORTED) {  // only allows first 2 instances of ID & age to be displayed
                                    int someInt = res.getInt(col);
                                    switch (i) {
                                        case 3:
                                            col = "REPORT_ID";  // format report ID
                                            break;
                                        case 8: // we actually want user ID here but would otherwise get report ID
                                            col = "\n-------------USER INFO------------\nUSER_ID";  // formats the printout with a linebreak
                                            someInt = res.getInt("reported_by");  // substitute value
                                            break;
                                        default:
                                            col = col.toUpperCase();  // no handling needed for age
                                            break;
                                    }
                                    System.out.println(col + ": " + someInt);  // display the line
                                    id_count++; // add to the id count in order to trigger threshold
                                }
                                break;
                            // handling timestamp to string conversion
                            case "timestamptz":
                                Timestamp time = res.getTimestamp(col);
                                System.out.println(col.toUpperCase() + ": " + time.toString());
                                break;
                            // handling geometry to string conversion
                            case "geometry":
                                PGgeometry geom = (PGgeometry) res.getObject(col);
                                System.out.println("LOCATION: " + geom.toString());
                                break;
                            default:
                                // if it's not one of the above, we just null check and format
                                String someString = res.getString(col);
                                if (someString != null) {
                                    System.out.println(col.toUpperCase() + ": " + someString);
                                }
                                break;
                        }
                    }
                    id_count = 0;  // reset counter for the next loop
                    System.out.println(String.format(headerFooter + "\n\n\n", "END"));  // concat within a format clause?  now I've seen it all.
                }
            } else {
                context.log("NULL QUERY RESULT DETECTED");  // failure message
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

} // fin