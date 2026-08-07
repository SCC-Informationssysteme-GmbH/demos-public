package com.example.mcpdemo.kundenverwaltung.mcpserver.tools;

import com.example.mcpdemo.kundenverwaltung.common.CustomerDto;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CustomerTools {

    private final RestClient domainApiRestClient;

    public CustomerTools(RestClient domainApiRestClient) {
        this.domainApiRestClient = domainApiRestClient;
    }

    @McpTool(name = "list_customers", description = "Listet alle Kunden der Kundenverwaltung auf")
    public List<CustomerDto> listCustomers() {
        return domainApiRestClient.get()
                .uri("/api/customers")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CustomerDto>>() {
                });
    }

    @McpTool(name = "get_customer", description = "Liefert Details zu einem Kunden anhand seiner ID")
    public CustomerDto getCustomer(@McpToolParam(description = "ID des Kunden", required = true) Long customerId) {
        return domainApiRestClient.get()
                .uri("/api/customers/{id}", customerId)
                .retrieve()
                .body(CustomerDto.class);
    }
}
