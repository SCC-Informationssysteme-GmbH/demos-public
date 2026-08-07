package com.example.mcpdemo.buchhandlung.domainapi.web;

import com.example.mcpdemo.buchhandlung.common.OrderDto;
import com.example.mcpdemo.buchhandlung.common.OrderTotalDto;
import com.example.mcpdemo.buchhandlung.domainapi.entity.BookEntity;
import com.example.mcpdemo.buchhandlung.domainapi.entity.OrderEntity;
import com.example.mcpdemo.buchhandlung.domainapi.repository.BookRepository;
import com.example.mcpdemo.buchhandlung.domainapi.repository.OrderRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public OrderController(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public List<OrderDto> listOrders(@RequestParam(required = false) Long customerId) {
        List<OrderEntity> entities = customerId != null
                ? orderRepository.findByCustomerId(customerId)
                : orderRepository.findAll();
        return entities.stream().map(OrderController::toDto).toList();
    }

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(OrderController::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bestellung " + id + " nicht gefunden"));
    }

    @GetMapping("/total")
    public OrderTotalDto getTotal(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<OrderEntity> orders = customerId != null
                ? orderRepository.findByCustomerId(customerId)
                : orderRepository.findAll();

        List<OrderEntity> filtered = orders.stream()
                .filter(o -> from == null || !o.getOrderDate().isBefore(from))
                .filter(o -> to == null || !o.getOrderDate().isAfter(to))
                .toList();

        Map<Long, BookEntity> booksById = bookRepository.findAllById(
                        filtered.stream().map(OrderEntity::getBookId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(BookEntity::getId, book -> book));

        BigDecimal totalAmount = filtered.stream()
                .map(o -> booksById.get(o.getBookId()).getPrice().multiply(BigDecimal.valueOf(o.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderTotalDto(customerId, from, to, filtered.size(), totalAmount);
    }

    private static OrderDto toDto(OrderEntity entity) {
        return new OrderDto(entity.getId(), entity.getCustomerId(), entity.getBookId(), entity.getQuantity(), entity.getOrderDate());
    }
}
