package com.camping.repository;

import com.camping.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategory(String category);
    List<Product> findByAvailableForSaleTrue();
    List<Product> findByAvailableForRentalTrue();
    List<Product> findByRecommendedForContaining(String siteType);
    List<Product> findByActiveTrueOrderByRatingDesc();
    List<Product> findByCategoryAndActiveTrue(String category);
}
