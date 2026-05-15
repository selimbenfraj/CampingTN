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
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "budget_predictions")
public class BudgetPrediction {
    @Id
    private String id;

    private String userId;

    // Inputs
    private String governorate;
    private String siteType; // FOREST, COASTAL, DESERT
    private int numberOfPersons;
    private int numberOfDays;
    private LocalDate startDate;
    private String season; // SPRING, SUMMER, AUTUMN, WINTER
    private String accommodationType; // BUILDING, TENT

    // Weather at time of prediction
    private double temperature;
    private double humidity;

    // Predicted outputs
    private double predictedBudget;
    private double budgetMin;
    private double budgetMax;
    private Map<String, Double> budgetBreakdown; // accommodation, food, transport, equipment, misc

    // Recommended equipment
    private List<String> recommendedEquipment;
    private List<String> essentialItems;
    private List<String> optionalItems;

    // Tips
    private List<String> tips;
    private String weatherAlert;

    @CreatedDate
    private LocalDateTime createdAt;
}
