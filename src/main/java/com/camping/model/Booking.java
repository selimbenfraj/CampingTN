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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;

    private String userId;
    private String userEmail;
    private String campingCenterId;
    private String campingCenterName;
    private String governorate;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private int numberOfPersons;
    private String accommodationType; // BUILDING, TENT

    private double totalCost;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    private String specialRequests;
    private String emergencyContact;
    private String emergencyPhone;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
