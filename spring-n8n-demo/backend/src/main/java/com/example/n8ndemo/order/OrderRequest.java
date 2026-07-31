package com.example.n8ndemo.order;

import java.math.BigDecimal;

public record OrderRequest(String article, int quantity, BigDecimal amount, String customer) {
}
