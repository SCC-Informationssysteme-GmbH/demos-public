package com.example.mcpdemo.buchhandlung.common;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderTotalDto(Long customerId, LocalDate from, LocalDate to, int orderCount, BigDecimal totalAmount) {
}
