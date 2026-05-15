package com.camping.service;

import com.camping.model.*;
import com.camping.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrueOrderByRatingDesc();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getRecommendedProducts(String siteType) {
        return productRepository.findByRecommendedForContaining(siteType);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product updated) {
        return productRepository.findById(id).map(p -> {
            p.setName(updated.getName());
            p.setDescription(updated.getDescription());
            p.setCategory(updated.getCategory());
            p.setPrice(updated.getPrice());
            p.setRentalPricePerDay(updated.getRentalPricePerDay());
            p.setStockSale(updated.getStockSale());
            p.setStockRental(updated.getStockRental());
            p.setActive(updated.isActive());
            return productRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteProduct(String id) {
        productRepository.findById(id).ifPresent(p -> {
            p.setActive(false);
            productRepository.save(p);
        });
    }

    // ---- Orders ----

    public Order createOrder(Order order) {
        order.setStatus("PENDING");
        order.setPaymentStatus("PENDING");
        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(String id, String status) {
        return orderRepository.findById(id).map(o -> {
            o.setStatus(status);
            o.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(o);
        }).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Map<String, Object> getShopStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProducts", productRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.countByStatus("PENDING"));
        stats.put("processingOrders", orderRepository.countByStatus("PROCESSING"));
        double totalRevenue = orderRepository.findAll().stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount).sum();
        stats.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        return stats;
    }

    public List<Map<String, Object>> getCategoryStats() {
        return productRepository.findAll().stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("category", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                }).collect(Collectors.toList());
    }
}
