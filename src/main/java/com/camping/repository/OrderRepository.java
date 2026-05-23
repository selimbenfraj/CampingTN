package com.camping.repository;

import com.camping.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByStatus(String status);
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByStatus(String status);
}
