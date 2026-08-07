package com.example.mcpdemo.buchhandlung.domainapi.repository;

import com.example.mcpdemo.buchhandlung.domainapi.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByCustomerId(Long customerId);
}
