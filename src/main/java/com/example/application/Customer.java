package com.example.application;

public class Customer {
    private int customer_id;
    private String name;
    private String address;
    private String mobile_number;
    private String email;

    public Customer(int customer_id, String name, String address, String mobile_number, String email){
        this.customer_id = customer_id;
        this.name = name;
        this.address = address;
        this.mobile_number = mobile_number;
        this.email = email;
    }

    public Customer(){

    }

    public int getCustomer_id() { return customer_id; }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

