package com.example.ReportsDemo;

public class Statements {
    public static String makeUser = "INSERT INTO users (first_name, last_name, gender, age, blood_type, telephone, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static String makeReport = "INSERT INTO report (reported_by, message, report_type, disaster_type) VALUES (?, ?, ?, ?)";
}
