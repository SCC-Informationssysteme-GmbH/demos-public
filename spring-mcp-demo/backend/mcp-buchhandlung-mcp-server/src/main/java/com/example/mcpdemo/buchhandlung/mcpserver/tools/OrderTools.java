package com.example.mcpdemo.buchhandlung.mcpserver.tools;

import com.example.mcpdemo.buchhandlung.common.OrderDto;
import com.example.mcpdemo.buchhandlung.common.OrderTotalDto;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class OrderTools {

    private final RestClient domainApiRestClient;

    public OrderTools(RestClient domainApiRestClient) {
        this.domainApiRestClient = domainApiRestClient;
    }

    @McpTool(name = "list_orders_for_customer", description = "Listet alle Bestellungen eines Kunden in der Buchhandlung auf")
    public List<OrderDto> listOrdersForCustomer(@McpToolParam(description = "ID des Kunden", required = true) Long customerId) {
        return domainApiRestClient.get()
                .uri("/api/orders?customerId={customerId}", customerId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OrderDto>>() {
                });
    }

    @McpTool(
            name = "get_total_spent",
            description = "Berechnet serverseitig (nicht vom Modell geschaetzt) die Gesamtsumme "
                    + "(Menge x Buchpreis) der Bestellungen, optional gefiltert nach Kunde und "
                    + "Zeitraum. Bei jeder Frage nach Summen/Gesamtausgaben immer dieses Tool "
                    + "nutzen statt selbst zu rechnen."
    )
    public OrderTotalDto getTotalSpent(
            @McpToolParam(description = "ID des Kunden, optional - ohne Angabe wird ueber alle Kunden summiert", required = false) Long customerId,
            @McpToolParam(description = "Startdatum inklusive (Format YYYY-MM-DD), optional", required = false) LocalDate from,
            @McpToolParam(description = "Enddatum inklusive (Format YYYY-MM-DD), optional", required = false) LocalDate to) {
        return domainApiRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/orders/total")
                        .queryParamIfPresent("customerId", Optional.ofNullable(customerId))
                        .queryParamIfPresent("from", Optional.ofNullable(from))
                        .queryParamIfPresent("to", Optional.ofNullable(to))
                        .build())
                .retrieve()
                .body(OrderTotalDto.class);
    }
}
