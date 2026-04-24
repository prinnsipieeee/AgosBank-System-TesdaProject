package com.agosbank.models;

public class User {
    private int  id;
    private String fullName;
    private String accountId;
    private String phoneNumber;
    private String pinCode;
    private double balance;

    public User(int id, String fullName, String accountId, String phoneNumber, String pinCode, double balance) {
        this.id = id;
        this.fullName = fullName;
        this.accountId = accountId;
        this.phoneNumber = phoneNumber;
        this.pinCode = pinCode;
        this.balance = balance;
    }

    public User() {
    }
    public int getId() {return id; }
    public String getFullName() {return fullName; }
    public void setFullName(String fullName) {this.fullName = fullName; }

    public String getAccountId(){return accountId; }
    
    public String getPhoneNumber(){return phoneNumber;}
    public void setPhoneNumber(String phoneNumber){this.phoneNumber = phoneNumber; }

    public String getPinCode(){return pinCode; }
    public void setPinCode(String pinCode){this.pinCode = pinCode; }

    public double getBalance(){return balance; }
    public void setBalance(double balance){this.balance = balance; }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
