package com.example.n8ndemo.order;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final N8nWebhookClient n8nWebhookClient;

    public OrderService(OrderRepository orderRepository, N8nWebhookClient n8nWebhookClient) {
        this.orderRepository = orderRepository;
        this.n8nWebhookClient = n8nWebhookClient;
    }

    public Order createOrder(OrderRequest request) {
        Order order = new Order(request.article(), request.quantity(), request.amount(), request.customer());
        order = orderRepository.save(order);

        order.setStatus(OrderStatus.PENDING_APPROVAL);
        order = orderRepository.save(order);

        n8nWebhookClient.notifyOrderCreated(order);
        return order;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order nicht gefunden: " + id));
    }

    public Order applyCallback(Long id, OrderStatus decision) {
        Order order = findById(id);
        order.setStatus(decision);
        return orderRepository.save(order);
    }
}
