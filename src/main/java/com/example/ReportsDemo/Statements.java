package com.example.ReportsDemo;

public class Statements {
    public static String makeUser = "INSERT INTO users (first_name, last_name, gender, email, age, telephone, blood_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static String makeReport = "INSERT INTO report (geom, message, report_type, disaster_type, reported_by) VALUES (?, ?, ?, ?, ?)";
    public static String getUser = "SELECT id FROM users WHERE email = ? LIMIT 1";
}
