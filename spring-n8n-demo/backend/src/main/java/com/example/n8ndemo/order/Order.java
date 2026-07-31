package com.example.n8ndemo.order;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String article;
    private int quantity;
    private BigDecimal amount;
    private String customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW;

    private Instant createdAt = Instant.now();

    protected Order() {
    }

    public Order(String article, int quantity, BigDecimal amount, String customer) {
        this.article = article;
        this.quantity = quantity;
        this.amount = amount;
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public String getArticle() {
        return article;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
