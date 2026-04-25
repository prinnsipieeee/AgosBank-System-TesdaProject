package com.agosbank.services;

import com.agosbank.database.DBConnection;
import java.sql.*;;

public class TransactionService{
    public boolean deposit(int userId, double amount){
        String updateBalanceSQL = "UPDATE users SET balance = balance + ? WHERE id=?";
        String logTransactionSQL = "INSERT INTO transaction (amount, transaction_type, account_id) VALUE (?, 'CASH IN', ?)";

        try (Connection conn = DBConnection.getConnection()){
            conn.setAutoCommit(false);
    
        try (PreparedStatement updstmt = conn.prepareStatement(updateBalanceSQL);
            PreparedStatement logstmt = conn.prepareStatement(logTransactionSQL)){
            
            updstmt.setDouble(1, amount);
            updstmt.setDouble(2, amount);
            updstmt.executeUpdate();

            logstmt.setDouble(1, amount);
            logstmt.setInt(2, userId);
            logstmt.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e){
                conn.rollback();
                System.out.println("Transacton Error: " + e.getMessage());
            }
        } catch (SQLException e){
            System.out.println("Connection Error: " + e.getMessage());
        } 
        return false;
    }

    public boolean sendMoney(int senderId, String receiverAccountId, double amount){
        String deductSenderSQL = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String addReceiveSQL = "UPDATE users SET balance = balance + ? WHERE account_id = ?";
        String loginTransactionSQL = "INSERT INTO transaction (amount, transaction_type, account_id, transferTOID, transferFromID) VALUES (?, 'TRANSFER', ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()){
            conn.setAutoCommit(false);

            try(PreparedStatement deductstmt = conn.prepareStatement(deductSenderSQL);
                PreparedStatement addrstamt = conn.prepareStatement(addReceiveSQL);
                PreparedStatement loginstmt = conn.prepareStatement(loginTransactionSQL)){
                    
                deductstmt.setDouble(1, amount);
                deductstmt.setInt(2, senderId);
                deductstmt.setDouble(3, amount);
                int rowsAffected = deductstmt.executeUpdate();

                if(rowsAffected == 0){
                    throw new SQLException("Insufficient Balance or Sender not Found");
                }

                addrstamt.setDouble(1, amount);
                addrstamt.setString(2, receiverAccountId);
                int receiverFound = addrstamt.executeUpdate();

                if (receiverFound == 0){
                    throw new SQLException("Receiver Account ID not Found.");
                }
                
                loginstmt.setDouble(1, amount);
                loginstmt.setInt(2, senderId);
                loginstmt.setString(3, receiverAccountId);
                loginstmt.setString(4, String.valueOf(senderId));
                loginstmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Transfer Failed: " + e.getMessage());
                return false;
            } 
        } catch (SQLException e){
            System.out.println("Connection Error: " + e.getMessage());
            return true; 
        }
    }
}