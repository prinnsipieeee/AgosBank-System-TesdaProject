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

    public Transaction(int id, double amount, String name, String accountId, Timestamp date, String transferToId, String transferFromId){
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.accountId = accountId;
        this.date = date;
        this.transferToId = transferToId;
        this.transferFromId = transferFromId;
    }

    public Transaction() {
    }
    public int getId()
    {return id;}
    public double  getAmount()
    {return amount;}
    public String getName()
    {return name;}
    public String gatAccountId()
    {return accountId;}
    public Timestamp getDate()
    {return date;}
    public String getTransferToId()
    {return transferToId;}
    public String getTransferFromId()
    {return transferFromId;}

}
