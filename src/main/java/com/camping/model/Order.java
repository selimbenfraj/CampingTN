package com.camping.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId;
    private String userEmail;

    private List<OrderItem> items;

    private double subtotal;
    private double tax;
    private double deliveryFee;
    private double totalAmount;

    private String status; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    private String orderType; // SALE, RENTAL

    // Rental dates
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;

    // Delivery
    private String deliveryAddress;
    private String deliveryGovernorate;

    // Payment
    private String paymentMethod; // CASH_ON_DELIVERY, CARD, TRANSFER
    private String paymentStatus; // PENDING, PAID, REFUNDED

    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private String productName;
        private int quantity;
        private double unitPrice;
        private double total;
        private boolean isRental;
        private int rentalDays;
    }
}
