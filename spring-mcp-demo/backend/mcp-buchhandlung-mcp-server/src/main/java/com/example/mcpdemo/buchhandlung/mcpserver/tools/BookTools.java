package com.example.mcpdemo.buchhandlung.mcpserver.tools;

import com.example.mcpdemo.buchhandlung.common.BookDto;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class BookTools {

    private final RestClient domainApiRestClient;

    public BookTools(RestClient domainApiRestClient) {
        this.domainApiRestClient = domainApiRestClient;
    }

    @McpTool(name = "list_books", description = "Listet alle Buecher der Buchhandlung auf")
    public List<BookDto> listBooks() {
        return domainApiRestClient.get()
                .uri("/api/books")
                .retrieve()
                .body(new ParameterizedTypeReference<List<BookDto>>() {
                });
    }

    @McpTool(name = "get_book", description = "Liefert Details zu einem Buch anhand seiner ID")
    public BookDto getBook(@McpToolParam(description = "ID des Buchs", required = true) Long bookId) {
        return domainApiRestClient.get()
                .uri("/api/books/{id}", bookId)
                .retrieve()
                .body(BookDto.class);
    }
}
