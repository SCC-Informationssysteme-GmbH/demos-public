package com.example.mcpdemo.buchhandlung.common;

import java.math.BigDecimal;

public record BookDto(Long id, String isbn, String title, String author, BigDecimal price) {
}
