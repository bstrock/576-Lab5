package com.example.ReportsDemo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DBUtility {

    public static String url = makeURL(
            "postgresql",
            "localhost",
            "brianstrock",
            5432
    );

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

    public static Connection connect(String url, HttpServletRequest request) throws SQLException, IOException {

        try {
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

    public static ArrayList<HashMap<String, String[]>> requestParamsToArrayList(HttpServletRequest request) {

        Map<String, String[]> params = request.getParameterMap();

        HashMap<String, String[]> userTable = new HashMap<>();
        HashMap<String, String[]> reportTable = new HashMap<>();
        HashMap<String, String[]> subReportTable = new HashMap<>();

        ArrayList<HashMap<String, String[]>> insertAttributes = new ArrayList<>();

        String[] keys = (String[]) params.keySet().toArray();
        for (int i = 1; i < keys.length; i++) {
            String key = keys[i];
            String[] val = params.get(key);
            if (i < 9) {
                userTable.put(key, val);
            } else if (i < 13) {
                reportTable.put(key, val);
            } else {
                subReportTable.put(key, val);
            }
        }

        insertAttributes.add(userTable);
        insertAttributes.add(reportTable);
        insertAttributes.add(subReportTable);

        return insertAttributes;
    }

    public static PreparedStatement insertParams(HashMap<String, String[]> tableAttributes, PreparedStatement raw) throws SQLException {

        for (String key : tableAttributes.keySet()) {

            int counter = 1;

            Object fieldInput = tableAttributes.get(key);
            try {
                int isInt = Integer.parseInt((String) fieldInput);
                raw.setInt(counter, isInt);
            } catch (NumberFormatException nfe) {
                try {
                    Timestamp timestamp = Timestamp.valueOf((String) fieldInput);
                    raw.setTimestamp(counter, timestamp);
                } catch (SQLException e) {
                    raw.setString(counter, (String) fieldInput);
                }
            }

        }
        return raw;
    }

    public static void createReport(HttpServletRequest request) throws SQLException, IOException {
        ServletContext context = request.getServletContext();

        context.log("INITIATING DB CONNECTION");
        Connection conn = DBUtility.connect(DBUtility.url, request);
        context.log("DB CONNECTION SUCCESSFUL");

        context.log("UNPACKING PARAMETERS");
        ArrayList<HashMap<String, String[]>> params = requestParamsToArrayList(request);
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
        PreparedStatement userSQL = insertParams(params.get(0), rawUser);
        PreparedStatement reportSQL = insertParams(params.get(1), rawReport);
        context.log("STATEMENT PARAMETERS ADDED");

        context.log("PREPARING EXECUTION");
        ArrayList<PreparedStatement> statements = new ArrayList<>();
        statements.add(userSQL);
        statements.add(reportSQL);

        try {
            for (PreparedStatement s : statements) {
                context.log("STATEMENT EXECUTED");
                s.executeUpdate();
                s.close();
            }
        } catch (SQLException e) {
            context.log("STATEMENT EXECUTION FAILURE");
            e.printStackTrace();
        } finally {
            conn.close();
        }
    }

    public static String makeURL(String driver,
                                 String host,
                                 String db,
                                 int port){

        return String.format("jdbc:%s://%s:%d/%s", driver, host, port, db);
    }

} // ends main class