package com.camping.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    private String name;
    private String description;
    private String category; // TENT, SLEEPING_BAG, COOKING, NAVIGATION, CLOTHING, SAFETY, HYDRATION
    private double price;
    private double rentalPricePerDay;

    @Builder.Default
    private boolean availableForSale = true;
    @Builder.Default
    private boolean availableForRental = true;

    private int stockSale;
    private int stockRental;

    private List<String> images;
    private String brand;
    private String sku;

    // Specs
    private String weight;
    private String dimensions;
    private String material;

    // Ratings
    private double rating;
    private int reviewCount;

    // Recommended for which site types
    private List<String> recommendedFor; // FOREST, COASTAL, DESERT

    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;
}
