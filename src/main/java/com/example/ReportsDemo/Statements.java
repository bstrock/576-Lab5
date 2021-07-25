package com.example.ReportsDemo;

public class Statements {
    public static String makeUser = "INSERT INTO users (first_name, last_name, gender, email, age, telephone, blood_type, emergency_contact_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    public static String makeReport = "INSERT INTO report (geom, message, report_type, disaster_type, reported_by) VALUES (?, ?, ?, ?, ?)";
    public static String getUser = "SELECT id FROM users WHERE email = ? LIMIT 1";
    public static String getReport = "SELECT id, (SELECT MAX(timestamp)) FROM report WHERE reported_by = ? GROUP BY report.id ORDER BY timestamp DESC LIMIT 1";
    public static String makeReportType = "INSERT INTO %s (id, %s) VALUES (?, ?)";
    public static String makeContact = "INSERT INTO users (first_name, last_name, email, telephone) VALUES (?, ?, ?, ?)";
}
