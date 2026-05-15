package com.camping.repository;

import com.camping.model.BudgetPrediction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BudgetPredictionRepository extends MongoRepository<BudgetPrediction, String> {
    List<BudgetPrediction> findByUserId(String userId);
    List<BudgetPrediction> findByGovernorate(String governorate);
}
