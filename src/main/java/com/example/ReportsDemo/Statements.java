package com.example.ReportsDemo;

import java.sql.SQLException;

public class Statements {

    // declare parameterize-able SQL statements
    private static final String makeUser = "INSERT INTO users (first_name, last_name, gender, email, age, telephone, blood_type, emergency_contact_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String makeReport = "INSERT INTO report (geom, message, report_type, disaster_type, reported_by) VALUES (?, ?, ?, ?, ?)";
    private static final String getUser = "SELECT id FROM users WHERE email = ? LIMIT 1";
    private static final String getReport = "SELECT id, (SELECT MAX(timestamp)) FROM report WHERE reported_by = ? GROUP BY report.id ORDER BY timestamp DESC LIMIT 1";
    private static final String makeReportType = "INSERT INTO %s (id, %s) VALUES (?, ?)";
    private static final String makeContact = "INSERT INTO users (first_name, last_name, email, telephone) VALUES (?, ?, ?, ?)";
    private static final String getAllReports = "SELECT * FROM report\n" +
            "FULL OUTER JOIN users\n" +
            "ON users.id = report.reported_by\n" +
            "FULL OUTER JOIN damage_report AS dam\n" +
            "ON report.id = dam.id\n" +
            "FULL OUTER JOIN request_report AS req\n" +
            "ON report.id = req.id\n" +
            "FULL OUTER JOIN donation_report AS don\n" +
            "ON report.id = don.id\n" +
            "WHERE users.gender IS NOT NULL\n" +
            "GROUP BY users.id, report.timestamp, report.geom, report.id, dam.id, req.id, don.id";

    public static String get(String type) throws SQLException {
        // use one public getter method which accepts a string and returns the matching statement
        String statement = null;

        switch (type) {
            case "fetchUser":
                statement = getUser;
                break;
            case "fetchReport":
                statement = getReport;
                break;
            case "makeUser":
                statement = makeUser;
                break;
            case "makeReport":
                statement = makeReport;
                break;
            case "makeReportType":
                statement = makeReportType;
                break;
            case "makeContact":
                statement = makeContact;
                break;
            case "getAllReports":
                statement = getAllReports;
        }
        return statement;
    }

}