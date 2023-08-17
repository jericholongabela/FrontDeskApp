package com.example.application;

public class Transaction {
    private int transaction_id;
    private String date_stored;
    private String date_retrieved;
    private String status;
    private int storage_id;
    private int customer_id;

    public Transaction(int transaction_id, String date_stored, String date_retrieved, String status, int storage_id, int customer_id) {
        this.transaction_id = transaction_id;
        this.date_stored = date_stored;
        this.date_retrieved = date_retrieved;
        this.status = status;
        this.storage_id = storage_id;
        this.customer_id = customer_id;
    }

    public Transaction (){

    }

    public int getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(int transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getDate_stored() {
        return date_stored;
    }

    public void setDate_stored(String date_stored) {
        this.date_stored = date_stored;
    }

    public String getDate_retrieved() {
        return date_retrieved;
    }

    public void setDate_retrieved(String date_retrieved) {
        this.date_retrieved = date_retrieved;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStorage_id() {
        return storage_id;
    }

    public void setStorage_id(int storage_id) {
        this.storage_id = storage_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }
}
