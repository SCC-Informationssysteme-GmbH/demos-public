package com.example.mcpdemo.kundenverwaltung.domainapi.repository;

import com.example.mcpdemo.kundenverwaltung.domainapi.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
}
