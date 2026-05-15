package com.camping.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BudgetDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionRequest {
        private String governorate;
        private String siteType; // FOREST, COASTAL, DESERT
        private int numberOfPersons;
        private int numberOfDays;
        private LocalDate startDate;
        private String accommodationType; // BUILDING, TENT
        private double budget; // optional user budget constraint
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionResponse {
        private String governorate;
        private String siteType;
        private int numberOfPersons;
        private int numberOfDays;
        private double predictedBudget;
        private double budgetMin;
        private double budgetMax;
        private Map<String, Double> budgetBreakdown;
        private List<String> recommendedEquipment;
        private List<String> essentialItems;
        private List<String> optionalItems;
        private List<String> tips;
        private String weatherSummary;
        private String weatherAlert;
        private double temperature;
        private double humidity;
        private String season;
    }
}
