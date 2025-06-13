package com.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderWithUser {
    private int id;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String userName;

    public OrderWithUser(int id, BigDecimal totalAmount, LocalDateTime orderDate, String userName) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.userName = userName;
    }

    // Getters...
    public int getId() {
        return id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public String getUserName() {
        return userName;
    }
}
