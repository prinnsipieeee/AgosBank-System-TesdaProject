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
            updstmt.setInt(2, userId);
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

    public void showHistory(int userId){
        String sql = "SELECT * FROM transaction WHERE account_id = ? ORDER BY date DESC";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n========= 🧾 TRANSACTION HISTORY =========");
            System.out.printf("%-15s | %-12s | %-20s\n", "TYPE", "AMOUNT", "DATE");
            System.out.println("------------------------------------------");

            boolean hasTransactions = false;
            while(rs.next()){
                hasTransactions = true;
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                String date = rs.getString("date");

                System.out.printf("%-15s | ₱%-11.2f | %-20s\n", type, amount, date);
            }
            if(!hasTransactions){
                System.out.println("No transaction found yet. ");
            }
            System.out.println("===================================\n");
        } catch(SQLException e){
            System.out.println("History Error: " + e.getMessage());
        }
    }

    public boolean withdraw(int userId, double amount){
        String updateSQL = "UPDATE users SET balance = balance - ? WHERE id = ? AND BALANCE >= ?";
        String logSQL = "INSERT INTO transaction (amount, transaction_type, account_id) VALUES (?, 'WITHDRAW', ?)";

        try(Connection conn = DBConnection.getConnection()){
            conn.setAutoCommit(false);

            try (PreparedStatement updatestmt = conn.prepareStatement(updateSQL);
                PreparedStatement logstmt = conn.prepareStatement(logSQL)) {
                
                updatestmt.setDouble(1, amount);
                updatestmt.setInt(2, userId);
                updatestmt.setDouble(3, amount);
                int affected = updatestmt.executeUpdate();

                if(affected == 0){
                    throw new SQLException("Insuficcient Balance or User not Found.");
                }

                logstmt.setDouble(1, amount);
                logstmt.setInt(2, userId);
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
}
