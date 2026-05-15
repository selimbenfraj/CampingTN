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
@Document(collection = "maintenance_tasks")
public class MaintenanceTask {
    @Id
    private String id;

    private String title;
    private String description;
    private String category; // WEBSITE, DATABASE, ML_MODEL, INFRASTRUCTURE, CONTENT
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    private String assignedTo;
    private String reportedBy;

    private LocalDate dueDate;
    private LocalDate resolvedDate;

    private String resolution;
    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
