package com.example.mcpdemo.buchhandlung.common;

import java.time.LocalDate;

public record OrderDto(Long id, Long customerId, Long bookId, Integer quantity, LocalDate orderDate) {
}
