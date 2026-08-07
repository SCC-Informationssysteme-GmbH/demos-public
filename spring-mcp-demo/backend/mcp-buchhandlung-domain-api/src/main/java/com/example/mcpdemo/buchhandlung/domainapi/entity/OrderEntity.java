package com.example.mcpdemo.buchhandlung.domainapi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Long bookId;
    private Integer quantity;
    private LocalDate orderDate;

    protected OrderEntity() {
    }

    public OrderEntity(Long customerId, Long bookId, Integer quantity, LocalDate orderDate) {
        this.customerId = customerId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.orderDate = orderDate;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getBookId() {
        return bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }
}
