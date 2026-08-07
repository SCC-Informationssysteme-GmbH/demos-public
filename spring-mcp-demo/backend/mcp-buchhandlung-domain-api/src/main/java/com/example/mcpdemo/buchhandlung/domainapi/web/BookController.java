package com.example.mcpdemo.buchhandlung.domainapi.web;

import com.example.mcpdemo.buchhandlung.common.BookDto;
import com.example.mcpdemo.buchhandlung.domainapi.entity.BookEntity;
import com.example.mcpdemo.buchhandlung.domainapi.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public List<BookDto> listBooks() {
        return bookRepository.findAll().stream().map(BookController::toDto).toList();
    }

    @GetMapping("/{id}")
    public BookDto getBook(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(BookController::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buch " + id + " nicht gefunden"));
    }

    private static BookDto toDto(BookEntity entity) {
        return new BookDto(entity.getId(), entity.getIsbn(), entity.getTitle(), entity.getAuthor(), entity.getPrice());
    }
}
