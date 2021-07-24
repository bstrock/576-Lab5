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
    public static final int REPORT_CUTOFF = 13; // 8-12 are report attributes

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

        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    protected static void printReports(ResultSet res) throws SQLException {

        if (res !=null) {
            while (res.next()){

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
                } else {
                    subReportTable.put(key, val); // only triggers if i > report_cutoff
                }
            }
            i ++; // first parameter is tab_id, so we skip that
        }

        // pack the sorted LinkedHashMaps into the container and return
        insertAttributes.add(userTable);
        insertAttributes.add(reportTable);
        insertAttributes.add(subReportTable);

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

        context.log("INITIATING DB CONNECTION");
        Connection conn = DBUtility.connect(DBUtility.url, request);
        context.log("DB CONNECTION SUCCESSFUL");
        ((org.postgresql.PGConnection)conn).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
        context.log("UNPACKING PARAMETERS");
        ArrayList<LinkedHashMap<String, String>> params = requestParamsToArrayList(request);
        context.log("PARAMETERS UNPACK SUCCESSFUL");

        context.log("RETRIEVING RAW SQL");
        String makeUser = Statements.makeUser;
        String makeReport = Statements.makeReport;
        context.log("SQL RETREIVE SUCCESSFUL");

        context.log("PREPARING STATEMENTS");
        PreparedStatement rawUser = conn.prepareStatement(makeUser);
        PreparedStatement rawReport = conn.prepareStatement(makeReport);
        context.log("PREPARED STATEMENTS CREATED");
        context.log("ADDING PARAMETERS TO STATEMENTS");
        PreparedStatement userSQL = insertParams(params.get(0), rawUser, 0);
        PreparedStatement reportSQL = insertParams(params.get(1), rawReport, 1);
        context.log("STATEMENT PARAMETERS ADDED");
        String email = params.get(0).get("email");
        context.log("EMAIL: " + email);
        PreparedStatement getUserID = conn.prepareStatement(Statements.getUser);
        getUserID.setString(1, params.get(0).get("email"));  // doesnt get the email
        int userID = 0;
        context.log(getUserID.toString());

        context.log("PREPARING EXECUTION");
        ArrayList<PreparedStatement> statements = new ArrayList<>();
        statements.add(userSQL);
        statements.add(getUserID);
        statements.add(reportSQL);

        try {
                userSQL.executeUpdate();
                ResultSet res = getUserID.executeQuery();

            context.log(String.valueOf(userID));
                if (res.next()) {
                    userID = res.getInt("id");
                }
                reportSQL.setInt(5, userID);
                context.log(reportSQL.toString());
                reportSQL.executeUpdate();

                for (PreparedStatement s : statements) {
                    s.close();
                }

                context.log("EXECUTION SUCCESSFUL");

        } catch (SQLException e) {
            context.log("STATEMENT EXECUTION FAILURE");
            context.log(e.toString());
            e.printStackTrace();
        } finally {
            conn.close();
        }
    }

    public static String makeURL(String driver,
                                 String host,
                                 String db,
                                 int port){

        return String.format("jdbc:%s://%s:%d/%s?stringtype=unspecified", driver, host, port, db);
    }

} // ends main class