package com.camping.controller;

import com.camping.dto.BudgetDTO;
import com.camping.service.BudgetPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetPredictionService budgetService;

    @PostMapping("/predict")
    public ResponseEntity<BudgetDTO.PredictionResponse> predict(
            @RequestBody BudgetDTO.PredictionRequest request,
            Authentication auth) {
        String userId = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(budgetService.predict(request, userId));
    }
}
