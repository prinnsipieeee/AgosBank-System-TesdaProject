package com.agosbank.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.agosbank.database.DBConnection;
import com.agosbank.models.User;

public class AuthService{
    private Connection getConnection() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASS");

        if (url == null || url.isBlank()) {
            throw new SQLException("Database URL not configured. Set DB_URL environment variable.");
        }

        return DriverManager.getConnection(url, user, password);
    }

    public boolean registerUser(String fullName, String accountId, String phoneNumber, String pinCode) {
        
        if (pinCode.length() != 4) {
            System.out.println("Registration Failed: PIN must be exactly 4 digits.");
            return false;
        }
        String sql = "INSERT INTO users (full_name, account_id, phone_number, pin_code, balance) VALUES (?, ?, ?, ?, 0.00)";
        
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement p = conn.prepareStatement(sql)){
                
            p.setString(1, fullName);
            p.setString(2, accountId);
            p.setString(3, phoneNumber);
            p.setString(4, pinCode);

            int rowsInserted = p.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.out.println("Registration Error: " + e.getMessage());
            return false;
        }
    }

    public User loginUser(String phoneNumber, String pinCode){
        String sql = "SELECT * FROM USERS WHERE phone_number = ? AND pin_code = ?";

        System.out.println("DEBUG: Connecting to Database...");

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement p = conn.prepareStatement(sql)){
                System.out.println("DEBUG: Connection Success!");

                p.setString(1, phoneNumber);
                p.setString(2, pinCode);

                ResultSet rs = p.executeQuery();
                
                if(rs.next()){
                    return new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("account_id"),
                        rs.getString("phone_number"),
                        rs.getString("pin_code"),
                        rs.getDouble("balance")
                    );
                } 
            } catch (SQLException e) {
                System.out.println("LOGIN ERROR!" + e.getMessage());
            }
            return null;
        }
}

