package com.agosbank.models;

import java.sql.Timestamp;

public class Transaction {
    private int id;
    private double amount;
    private String transactionType; 
    private String senderName;      
    private String receiverName;   
    private String accountId;
    private Timestamp date;
    private String transferToId;
    private String transferFromId;

    public Transaction(int id, double amount, String transactionType, String senderName, String receiverName, 
                       String accountId, Timestamp date, String transferToId, String transferFromId) {
        this.id = id;
        this.amount = amount;
        this.transactionType = transactionType;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.accountId = accountId;
        this.date = date;
        this.transferToId = transferToId;
        this.transferFromId = transferFromId;
    }

    public Transaction() {}

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getTransactionType() { return transactionType; }
    public String getSenderName() { return senderName; }
    public String getReceiverName() { return receiverName; }
    public String getAccountId() { return accountId; }
    public Timestamp getDate() { return date; }
    public String getTransferToId() { return transferToId; }
    public String getTransferFromId() { return transferFromId; }
}