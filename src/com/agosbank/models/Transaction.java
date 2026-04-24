package com.agosbank.models;
import java.sql.Timestamp;

public class Transaction {
    private int id;
    private double amount;
    private String name;
    private String accountId;
    private Timestamp date;
    private String transferToId;
    private String transferFromId;

    public Transaction(int id, double amount, String name, String accountId, Timestamp date, String transferToId, String TransferFromId){
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.accountId = accountId;
        this.date = date;
        this.transferFromId = transferToId;
        this.transferFromId = transferFromId;
    }

}
