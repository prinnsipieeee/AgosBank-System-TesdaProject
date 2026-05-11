package com.agosbank.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.agosbank.database.DBConnection;
import com.agosbank.models.Transaction;
;

public class TransactionService{

    public boolean deposit(String accountId, double amount, String sourceName) {
        // 1. SQL Queries base sa screenshot mo
        String updateBalanceSQL = "UPDATE users SET balance = balance + ? WHERE account_id = ?";
        
        // Ginagamit ang 'name' para sa source at 'account_ID' para sa recipient
        String logTransactionSQL = "INSERT INTO transaction (amount, transaction_type, sender_name, account_ID) VALUES (?, 'CASH IN', ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            
            conn.setAutoCommit(false); 

            try (PreparedStatement updstmt = conn.prepareStatement(updateBalanceSQL);
                PreparedStatement logstmt = conn.prepareStatement(logTransactionSQL)) {
                
                // Step A: Update Balance sa 'users' table
                updstmt.setDouble(1, amount);
                updstmt.setString(2, accountId); 
                int rowsAffected = updstmt.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback(); 
                    return false; 
                }

                // Step B: Log the Transaction sa 'transaction' table (Match sa DB mo)
                logstmt.setDouble(1, amount);           // amount
                logstmt.setString(2, sourceName);       // name (yung deposit source)
                logstmt.setString(3, accountId);        // account_ID
                logstmt.executeUpdate();

                conn.commit(); 
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Transaction Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
        } 
        return false;
    }

    public boolean validateRecipient(String accountId, String fullName, String mobileNum) {
        String sql = "SELECT * FROM users WHERE account_id = ? AND full_name = ? AND phone_number = ?";
        
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, mobileNum);
            
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next(); 
            
        } catch (SQLException e) {
            System.err.println("Validation Error: " + e.getMessage());
            return false;
        }
    }

    public boolean sendMoney(String senderAccId, String receiverAccId, double amount){
        String deductSenderSQL = "UPDATE users SET balance = balance - ? WHERE account_id = ? AND balance >= ?";
        String addReceiveSQL = "UPDATE users SET balance = balance + ? WHERE account_id = ?";
        String loginTransactionSQL = "INSERT INTO transaction (amount, transaction_type, sender_name, receiver_name, account_id, transferTOID, transferFromID) VALUES (?, 'SEND MONEY', ?, ?, ?, ?, ?)";
        String getNameSQL = "SELECT full_name FROM users WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection()){
            conn.setAutoCommit(false);

            String senderName = "Unknown Sender";
            String receiverName = "Unknown Receiver";

            try (PreparedStatement nameStmt = conn.prepareStatement(getNameSQL)) {
                nameStmt.setString(1, senderAccId);
                ResultSet rs = nameStmt.executeQuery();
                if (rs.next()) {
                    senderName = rs.getString("full_name");
                }
                nameStmt.setString(1, receiverAccId);
                ResultSet rs2 = nameStmt.executeQuery();
                if(rs2.next()){
                    receiverName = rs2.getString("full_name");
                }
            }

            try(PreparedStatement deductstmt = conn.prepareStatement(deductSenderSQL);
                PreparedStatement addrstamt = conn.prepareStatement(addReceiveSQL);
                PreparedStatement loginstmt = conn.prepareStatement(loginTransactionSQL)){
                    
                deductstmt.setDouble(1, amount);
                deductstmt.setString(2, senderAccId);
                deductstmt.setDouble(3, amount);
                int rowsAffected = deductstmt.executeUpdate();

                if(rowsAffected == 0){
                    throw new SQLException("Insufficient balance.");
                }

                addrstamt.setDouble(1, amount);
                addrstamt.setString(2, receiverAccId);
                int receiverFound = addrstamt.executeUpdate();

                if (receiverFound == 0){
                    throw new SQLException("Receiver Account ID not Found.");
                }
                
                loginstmt.setDouble(1, amount);
                loginstmt.setString(2, senderName);
                loginstmt.setString(3, receiverName);
                loginstmt.setString(4, senderAccId);
                loginstmt.setString(5, receiverAccId);
                loginstmt.setString(6, senderAccId);
                loginstmt.executeUpdate();

                conn.commit();
                System.out.println("History Saved: SEND MONEY from " + senderAccId + " to " + receiverAccId);
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Transfer Failed: " + e.getMessage());
                return false;
            } 
        } catch (SQLException e){
            System.out.println("Connection Error: " + e.getMessage());
            return false; 
        }
    }

    public List<Transaction> getFilteredHistory(String accountId, String filterType) {
        List<Transaction> list = new ArrayList<>();
        String dateQuery = "";
            switch (filterType) {
            case "TODAY":
                dateQuery = " AND DATE(date) = CURDATE()";
                break;
            case "THIS MONTH":
                dateQuery = " AND MONTH(date) = MONTH(CURDATE()) AND YEAR(date) = YEAR(CURDATE())";
                break;
            case "THIS YEAR":
                dateQuery = " AND YEAR(date) = YEAR(CURDATE())";
                break;
            default: // "ALL"
                dateQuery = ""; 
        }

        String sql = "SELECT * FROM transaction WHERE account_id = ?" + dateQuery+ " ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Transaction(
                    rs.getInt("id"),
                    rs.getDouble("amount"),
                    rs.getString("transaction_type"),
                    rs.getString("sender_name"),
                    rs.getString("receiver_name"),
                    rs.getString("account_id"),
                    rs.getTimestamp("date"),
                    rs.getString("transferTOID"),
                    rs.getString("transferFromID")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean withdraw(String accountId, double amount){
        String updateSQL = "UPDATE users SET balance = balance - ? WHERE id = ? AND BALANCE >= ?";
        String logSQL = "INSERT INTO transaction (amount, transaction_type, account_id) VALUES (?, 'WITHDRAW', ?)";

        try(Connection conn = DBConnection.getConnection()){
            conn.setAutoCommit(false);

            try (PreparedStatement updatestmt = conn.prepareStatement(updateSQL);
                PreparedStatement logstmt = conn.prepareStatement(logSQL)) {
                
                updatestmt.setDouble(1, amount);
                updatestmt.setString(2, accountId);
                updatestmt.setDouble(3, amount);
                int affected = updatestmt.executeUpdate();

                if(affected == 0){
                    throw new SQLException("Insuficcient Balance or User not Found.");
                }

                logstmt.setDouble(1, amount);
                logstmt.setString(2, accountId);
                logstmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e){
                conn.rollback();
                System.out.println("Connection Error: " + e.getMessage());
                return false;
            } 
        } catch (SQLException e){
                System.out.println("Connection Error: " + e.getMessage());
        } return false;
    }

    public double getUserBalance(String accountId) {
        double balance = 0.0;
        // Query base sa account_id na nasa database mo
        String query = "SELECT balance FROM users WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                balance = rs.getDouble("balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance;
    }
}
