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
@Document(collection = "camping_centers")
public class CampingCenter {
    @Id
    private String id;

    private int nb;
    private String name;
    private String governorate;
    private String region;
    private int yearCreated;
    private String phone;

    // Capacity
    private int capacityBuildings;
    private int capacityTents;
    private int capacityTotal;

    // Site type
    private String siteNature; // forestier, littoral, désertique

    // Coordinates
    private double latitude;
    private double longitude;

    // Additional info
    private String description;
    private List<String> amenities;
    private List<String> images;
    private double rating;
    private int reviewCount;

    // Availability
    @Builder.Default
    private boolean active = true;
    private int currentOccupancy;

    // Weather data (cached)
    private WeatherInfo weatherInfo;

    @CreatedDate
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherInfo {
        private double temperature;
        private double humidity;
        private double windSpeed;
        private String condition;
        private String icon;
        private LocalDateTime lastUpdated;
    }
}
