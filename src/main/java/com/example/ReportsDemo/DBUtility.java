package com.example.ReportsDemo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

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

    // these are readability variables used to set cutoffs for enumeration lengths, etc.

    public static final int USER_CUTOFF = 8; // attributes 1-8 are user table attributes
    public static final int REPORT_CUTOFF = 13; // 9-13 are report attributes
    public static final int CONTACT_CUTOFF = 14;
    public static final int HAS_EMERGENCY_CONTACT = 3;

    public static void main(String[] args) throws SQLException, IOException {
        /*
        // build url

        // initiate connection
        Connection conn = DBUtility.connect(DBUtility.url);

        // build query
        String query = "SELECT id, report_type, disaster_type, timestamp, ST_AsText(geom) as geom FROM report";

        // prepare statement
        PreparedStatement statement = conn.prepareStatement(query);

        // execute statement & print report
        ResultSet res = statement.executeQuery();
        printReports(res);

        // clean up
        statement.close();
        conn.close();
        */
    }

    public static Connection connect(String url, HttpServletRequest request) throws SQLException, IOException, ClassNotFoundException {
        // this function gets you a url to connect to
        try {
            Class.forName("org.postgis.DriverWrapper");  // load postgis
            ServletContext context = request.getServletContext();  // we use this for logging
            InputStream in = context.getResourceAsStream("/WEB-INF/db.properties");  // load database password, etc.

            Properties connProps = new Properties();  // initialize object for user/pass
            connProps.load(in);  // load user/pass from properties input stream

            return DriverManager.getConnection(url, connProps);

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

                String header = "********************\n";
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

        ServletContext context = request.getServletContext(); // use this for logging

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
        insertAttributes.add(emergencyContact);

        return insertAttributes;
    }

    public static PreparedStatement insertParams(LinkedHashMap<String, String> tableAttributes, PreparedStatement raw, int locationMode) throws SQLException {
        // this function packs the supplied key-value pairs into the supplied prepared statement
        // the statement can then be executed

        Logger log = Logger.getLogger("Reports");

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
        ((org.postgresql.PGConnection) conn).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
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

        // we check if the data package contains an emergency contact id
        // we also check if the contact user already exist in the database, so that we don't violate unique constraints

        if (params.size() > HAS_EMERGENCY_CONTACT) {  // this protocol engaged when emergency contact is specified
            context.log("EMERGENCY CONTACT DETECTED");
            String contactEmail = params.get(3).get("email");  // the emergency contact email
            PreparedStatement getContactID = conn.prepareStatement(Statements.get("fetchUser")); // prepare statement
            getContactID.setString(1, contactEmail);  // insert email in query string
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

        // we're going to check if we need to create a user, or use an existing one.
        // then we'll create the user (if needed), and create the report.
        // once we create the report, we'll get its ID and use that to populate the reportType table,
        // along with its secondary value (resource_type, disaster_type, etc. etc.) using linked table inheritance.

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

            context.log("CREATING REPORT");
            reportSQL.setInt(5, userID);  // set the userID in the report
            reportSQL.executeUpdate();  // execute create report statement

            getReportID.setInt(1, userID);  // we need to specify user ID in order to get the right reportID
            res = getReportID.executeQuery();  // get the reportID we just created

            if (res.next()) {  // set cursor
                reportID = res.getInt("id");  // set the reportID
                res.close();
                context.log("REPORT CREATED SUCCESSFULLY");
            }

            // we're going to handle the sub-report type (linked tables for disaster_report, etc.)
            context.log("CREATING SUB-REPORT");
            String type = params.get(1).get("report_type");  // get the report type
            String reportTypeTableName = type.toLowerCase() + "_report";  // set the DB tablename for this report type

            String typeColumn;  // we'll use this later

            // request report types have a different column name value, so we substitute here
            if (type.equals("REQUEST")) {
                typeColumn = "resource_type";
            } else {  // otherwise, type + _type = name of column
                typeColumn = type + "_type";
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

            String typeValue = params.get(2).get(typeColumn); // get the type for this report
            PreparedStatement reportTypeSQL = conn.prepareStatement(rawReportType);  // prepare report type statement
            reportTypeSQL.setInt(1, reportID);  // insert report ID value
            reportTypeSQL.setString(2, typeValue);  // insert type column value
            context.log("PARAMETERS ADDED TO SUB-REPORT");
            reportTypeSQL.executeUpdate();  // execute the statement
            statements.add(reportTypeSQL);  // add it to the statements list
            context.log("SUB-REPORT CREATED SUCCESSFULLY");

            for (PreparedStatement s : statements) {  // remember our statements list?  good times.
                s.close(); // let's close all the statements!
            }

            context.log("EXECUTION SUCCESSFUL");  // boom

        } catch (SQLException e) {
            context.log("STATEMENT EXECUTION FAILURE: CHECK SERVER LOGS FOR REFERENCE POINT");  //  aahhhhh!
            context.log(e.toString());
            e.printStackTrace();
            throw e;
        } finally {
            conn.close();  // close the connection regardless of success or the db will cry
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

    public static void queryReport(HttpServletRequest request, HttpServletResponse response) {
    }
} // fin