package com.agosbank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // PALITAN MO ITONG TATLONG LINES NA ITO:
    private static final String URL = "jdbc:mysql://localhost:3306/agos_bank_db"; 
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; // Karaniwang blanko ito sa XAMPP

    public static Connection getConnection() throws SQLException {
        try {
            // Siguraduhing may mysql-connector-j sa pom.xml mo
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found!", e);
        } catch (SQLException e) {
            // I-print ang exact error para alam natin kung mali ang password o DB name
            System.out.println("DB CONNECTION ERROR: " + e.getMessage());
            throw e;
        }
    }
}