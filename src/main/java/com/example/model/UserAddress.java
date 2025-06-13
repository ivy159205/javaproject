package com.example.model;

import java.time.LocalDateTime;

public class UserAddress {
    private int id;
    private int userId;
    private String address;
    private String city;
    private String postalCode;
    private LocalDateTime createdDate;

    public UserAddress() {
    }

    public UserAddress(int id, int userId, String address, String city, String postalCode) {
        this.id = id;
        this.userId = userId;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    public UserAddress(int userId, String address, String city, String postalCode) {
        this.userId = userId;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
