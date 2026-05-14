package com.agosbank.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.agosbank.database.DBConnection;
import com.agosbank.models.User;

public class AuthService{
    public boolean isIdentityTaken(String mobile, String email, String fullName) {
        String sql = "SELECT 1 FROM users WHERE phone_number = ? OR email = ? OR full_name = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, mobile);
            pstmt.setString(2, email);
            pstmt.setString(3, fullName);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); 
            }
        } catch (SQLException e) {
            System.err.println("Registration Check Error: " + e.getMessage());
            return false;
        } 
    }

    public boolean verifyForgotPinIdentity(String mobile, String email) {
        String sql = "SELECT 1 FROM users WHERE phone_number = ? AND email = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, mobile);
            pstmt.setString(2, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Forgot PIN Verification Error: " + e.getMessage());
            return false;
        } 
    }

    public boolean registerUser(String fullName, String accountId, String phoneNumber, String email, String pinCode) {
        
        if (pinCode == null || pinCode.length() != 4) {
        System.out.println("Registration Failed: PIN must be exactly 4 digits.");
            return false;
        }

        if (isIdentityTaken(phoneNumber, email, fullName)){
            System.out.println("Registration Failed: Name, Email, or Mobile Number is already registered.");
            return false;
        }

        String sql = "INSERT INTO users (full_name, account_id, phone_number, email, pin_code, balance) VALUES (?, ?, ?, ?, ?, 0.00)";
        
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement p = conn.prepareStatement(sql)){  
                
            p.setString(1, fullName);
            p.setString(2, accountId);
            p.setString(3, phoneNumber);
            p.setString(4, email);
            p.setString(5, pinCode);

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
                    java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                    String formattedDate = new SimpleDateFormat("MMMM yyyy").format(timestamp);
                    
                    return new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("account_id"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("pin_code"),
                        rs.getDouble("balance"),
                        formattedDate
                    );
                } 
            } catch (SQLException e) {
                System.out.println("LOGIN ERROR!" + e.getMessage());
            }
            return null;
        }
    
    public boolean changePin(String accountId, String oldPin, String newPin){
        String query = "UPDATE users SET pin_code = ? WHERE account_id = ? AND pin_code = ?";

        try (Connection conn = DBConnection.getConnection();
        PreparedStatement p = conn.prepareStatement(query)) {

            p.setString(1, newPin);
            p.setString(2, accountId);
            p.setString(3, oldPin);

            int affectedRows = p.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean resetPin(String accountId, String newPin) {
        String query = "UPDATE users SET pin_code = ? WHERE account_id = ?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement p = conn.prepareStatement(query)) {
            
            p.setString(1, newPin);
            p.setString(2, accountId);

            int affectedRows = p.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e){
            return false;
        }
    }

    public boolean updatePinByMobile(String mobile, String newPin) {
        String query = "UPDATE users SET pin_code = ? WHERE phone_number = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, newPin);
            pstmt.setString(2, mobile);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}   

