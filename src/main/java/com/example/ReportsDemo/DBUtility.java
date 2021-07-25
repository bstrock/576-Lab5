package com.example.ReportsDemo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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
            "postgresql_postGIS",
            "localhost",
            "brianstrock",
            5432
    );

    // PARAMETER MAP CUTOFF VALUES DECLARED HERE
    public static final int USER_CUTOFF = 8; // attributes 1-8 are user table attributes
    public static final int REPORT_CUTOFF = 13; // 9-13 are report attributes
    public static final int CONTACT_CUTOFF = 14;

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

        try {
            Class.forName("org.postgis.DriverWrapper");
            ServletContext context = request.getServletContext();
            InputStream in = context.getResourceAsStream("/WEB-INF/db.properties");

            Properties connProps = new Properties();  // initialize object for user/pass
            connProps.load(in);

            return DriverManager.getConnection(url, connProps);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    protected static void printReports(ResultSet res) throws SQLException {

        if (res != null) {
            while (res.next()) {

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
                System.out.println(header + body + header);
            }
        }
    }

    public static ArrayList<LinkedHashMap<String, String>> requestParamsToArrayList(HttpServletRequest request) {
        // this function sorts request parameters into an arraylist of LinkedHashMaps

        ServletContext context = request.getServletContext(); // use this for logging

        Enumeration<String> keys = request.getParameterNames();

        LinkedHashMap<String, String> userTable = new LinkedHashMap<>();  // where we store the user table key-values
        LinkedHashMap<String, String> reportTable = new LinkedHashMap<>();  // where we store the report table key-values
        LinkedHashMap<String, String> subReportTable = new LinkedHashMap<>();  // where we store the sub-report key-values
        LinkedHashMap<String, String> emergencyContact = new LinkedHashMap<>();

        ArrayList<LinkedHashMap<String, String>> insertAttributes = new ArrayList<>();  // THIS IS RETURNED

        // loop through keys, get attribute values, use cutoffs to place them in the correct LinkedHashMap
        int i = 0;  // starting counter value

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (i != 0) {
                String val = request.getParameter(key);

                context.log(String.format("i = %d, key = %s, val = %s", i, key, val));
                if (i < USER_CUTOFF) {  // goes in the user table map
                    userTable.put(key, val);
                } else if (i < REPORT_CUTOFF) { // only triggers if i > user_cutoff
                    reportTable.put(key, val);
                } else if (i < CONTACT_CUTOFF){
                    subReportTable.put(key, val); // only triggers if i > report_cutoff
                } else {
                    emergencyContact.put(key.replace("c_", ""), val);
                }
            }
            i++; // first parameter is tab_id, so we skip that
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
        // the statement can now be executed!
        Logger log = Logger.getLogger("Reports");

        int counter = 1;

        if (locationMode == 1) {
            String[] coords = new String[2];
            coords[0] = tableAttributes.get("longitude");
            coords[1] = tableAttributes.get("latitude");
            String geomString = String.format("POINT %s %s", coords[0], coords[1]);
            Geometry geom = PGgeometry.geomFromString(geomString);
            raw.setObject(counter, new PGgeometry(geom));
            tableAttributes.remove("latitude");
            tableAttributes.remove("longitude");
            counter++;
        }

        for (String key : tableAttributes.keySet()) {
            String fieldInput = tableAttributes.get(key);
            log.info(counter + " " + fieldInput);
            try {
                int isInt = Integer.parseInt((String.valueOf(fieldInput)));
                raw.setInt(counter, isInt);
            } catch (NumberFormatException nfe) {
                raw.setString(counter, fieldInput);
            }
            counter++;
        }

        return raw;
    }

    public static void createReport(HttpServletRequest request) throws SQLException, IOException, ClassNotFoundException {
        ServletContext context = request.getServletContext();
        int userID = 0, reportID = 0, contactID = 0; // initialize

        context.log("INITIATING DB CONNECTION");
        Connection conn = DBUtility.connect(DBUtility.url, request);
        context.log("DB CONNECTION SUCCESSFUL");
        ((org.postgresql.PGConnection) conn).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
        context.log("UNPACKING PARAMETERS");
        ArrayList<LinkedHashMap<String, String>> params = requestParamsToArrayList(request);
        context.log("PARAMETERS UNPACK SUCCESSFUL");

        context.log("PREPARING STATEMENTS");
        PreparedStatement rawUser = conn.prepareStatement(Statements.makeUser);
        PreparedStatement rawReport = conn.prepareStatement(Statements.makeReport);

        String email = params.get(0).get("email");
        PreparedStatement getUserID = conn.prepareStatement(Statements.getUser);
        getUserID.setString(1, email);

        context.log("PREPARED STATEMENTS CREATED");
        context.log("ADDING PARAMETERS TO STATEMENTS");
        PreparedStatement userSQL = insertParams(params.get(0), rawUser, 0);
        PreparedStatement reportSQL = insertParams(params.get(1), rawReport, 1);

        if (params.size() > 3){  // emergency contact is specified

            String contactEmail = params.get(3).get("email");  // the emergency contact email

            PreparedStatement getContactID = conn.prepareStatement(Statements.getUser); // prepare statement
            getContactID.setString(1, contactEmail);  // insert email in query string
            try {
                ResultSet res = getContactID.executeQuery();  // check for emergency contact in db
                if (res.next()) {  // if there's a result
                    contactID = res.getInt("id");  // get the ID and save
                    context.log(String.valueOf(contactID));
                    res.close();  // close resultset
                } else {  // if this is a new contact
                    String rawContact = Statements.makeContact;  // summon create emergency contact SQL
                    PreparedStatement makeContact = conn.prepareStatement(rawContact);  // prepare statement
                    PreparedStatement contactSQL = insertParams(params.get(3), makeContact, 0);  // insert values
                    contactSQL.executeUpdate();  // execute emergency contact creation
                    res = getContactID.executeQuery();  // retrieve contact ID we just created
                    if (res.next()) {  // if it was found
                        contactID = res.getInt("id"); // set contactID
                        res.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }

        }
        context.log("STATEMENT PARAMETERS ADDED");

        PreparedStatement getReportID = conn.prepareStatement(Statements.getReport);  // prepare get report ID

        context.log("PREPARING EXECUTION");
        // add statements to list for future operations
        ArrayList<PreparedStatement> statements = new ArrayList<>();
        statements.add(userSQL);
        statements.add(getUserID);
        statements.add(reportSQL);
        statements.add(getReportID);

        // CREATE REPORT EXECUTION BLOCK
        try {
            ResultSet res = getUserID.executeQuery();  // check if user exists

            if (res.next()) {  // if it does
                userID = res.getInt("id");  // set the userID
                res.close();
            } else {  // if it doesn't
                if (contactID != 0){  // if we found an emergency contact previously
                    userSQL.setInt(8, contactID);  // set the emergency contactID
                } else {  // if we didn't find a contact ID
                    userSQL.setNull(8, Types.NULL); // set it to NULL
                }
                userSQL.executeUpdate(); // execute create user statement
                res = getUserID.executeQuery();  // get the user ID we just created
                if (res.next()) {  // if we found it
                    userID = res.getInt("id");  // set userID
                    res.close();
                }
            }

            reportSQL.setInt(5, userID);  // set the userID in the report

            reportSQL.executeUpdate();  // execute create report statement

            getReportID.setInt(1, userID);  // specify user ID in order to get reportID
            res = getReportID.executeQuery();  // get the reportID we just created

            if (res.next()) {  // if we found it
                reportID = res.getInt("id");  // set the reportID
                res.close();
            }

            // SUB-REPORT TYPE HANDLING
            String type = params.get(1).get("report_type");  // get the report type
            String reportTypeTableName = type.toLowerCase() + "_report";  // DB tablename for this report type

            String typeColumn;  // we'll use this later

            // request report types have a different column name value, so we substitute here
            if (type.equals("REQUEST")){
                typeColumn = "resource_type";
            } else {  // otherwise, type + _type = name of column
                typeColumn = type + "_type";
            }

            // PreparedStatements wrap inserted values with '', which causes tablenames and such to fail...
            // thus, we use string.format to insert tablenames (these values are not supplied by the user)
            String rawReportType = String.format(Statements.makeReportType, reportTypeTableName, typeColumn);

            String typeValue = params.get(2).get(typeColumn); // get the type for this report

            PreparedStatement reportTypeSQL = conn.prepareStatement(rawReportType);  // prepare report type statement
            reportTypeSQL.setInt(1, reportID);  // insert report ID value
            reportTypeSQL.setString(2, typeValue);  // insert type column value

            reportTypeSQL.executeUpdate();  // execute the statement
            statements.add(reportTypeSQL);  // add it to the statements list

            for (PreparedStatement s : statements) {  // close all of the statements
                s.close();
            }

            context.log("EXECUTION SUCCESSFUL");  // boom

        } catch (SQLException e) {
            context.log("STATEMENT EXECUTION FAILURE");  //  aahhhhh!
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

        return String.format("jdbc:%s://%s:%d/%s?stringtype=unspecified", driver, host, port, db);
    }

} // ends main class