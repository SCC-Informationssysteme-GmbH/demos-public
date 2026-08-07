package com.example.mcpdemo.kundenverwaltung.domainapi.web;

import com.example.mcpdemo.kundenverwaltung.common.CustomerDto;
import com.example.mcpdemo.kundenverwaltung.domainapi.entity.CustomerEntity;
import com.example.mcpdemo.kundenverwaltung.domainapi.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public List<CustomerDto> listCustomers() {
        return customerRepository.findAll().stream().map(CustomerController::toDto).toList();
    }

    @GetMapping("/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(CustomerController::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kunde " + id + " nicht gefunden"));
    }

    private static CustomerDto toDto(CustomerEntity entity) {
        return new CustomerDto(entity.getId(), entity.getName(), entity.getEmail(), entity.getCity());
    }
}
