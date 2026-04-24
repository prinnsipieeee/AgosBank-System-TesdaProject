package com.agosbank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection{
    private static final String URL = "jdbc:mysql://localhost:3306/agos_bank_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(URL,USER,PASSWORD);

            System.out.println("AgosBank: Database conenction Established!");

        } catch (ClassNotFoundException e) {
            System.out.println("Error: MySQL Driver not found. Check your lib folder!");
        } catch (SQLException e) {
            System.out.println("Error: Failed to connect to database. Is XAMPP/MySQL running?");
        }
        return connection;
    }
}