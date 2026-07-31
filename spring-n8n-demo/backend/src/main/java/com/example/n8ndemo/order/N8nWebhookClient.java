package com.example.n8ndemo.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class N8nWebhookClient {

    private static final Logger log = LoggerFactory.getLogger(N8nWebhookClient.class);

    private final RestClient restClient;
    private final String orderCreatedUrl;

    public N8nWebhookClient(RestClient.Builder restClientBuilder,
                             @Value("${n8n.webhook.order-created-url}") String orderCreatedUrl) {
        this.restClient = restClientBuilder.build();
        this.orderCreatedUrl = orderCreatedUrl;
    }

    public void notifyOrderCreated(Order order) {
        try {
            restClient.post()
                    .uri(orderCreatedUrl)
                    .body(new OrderCreatedPayload(order))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("n8n-Webhook-Aufruf fehlgeschlagen fuer Order {}: {}", order.getId(), e.getMessage());
        }
    }

    private record OrderCreatedPayload(Long orderId, String article, int quantity,
                                        java.math.BigDecimal amount, String customer) {
        OrderCreatedPayload(Order order) {
            this(order.getId(), order.getArticle(), order.getQuantity(), order.getAmount(), order.getCustomer());
        }
    }
}
