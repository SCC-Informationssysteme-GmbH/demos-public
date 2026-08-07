package com.example.mcpdemo.buchhandlung.domainapi.repository;

import com.example.mcpdemo.buchhandlung.domainapi.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
}
