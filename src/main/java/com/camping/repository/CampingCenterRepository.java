package com.camping.repository;

import com.camping.model.CampingCenter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface CampingCenterRepository extends MongoRepository<CampingCenter, String> {
    List<CampingCenter> findByGovernorate(String governorate);
    List<CampingCenter> findBySiteNatureContainingIgnoreCase(String siteType);
    List<CampingCenter> findByRegion(String region);
    List<CampingCenter> findByActiveTrue();
    @Query("{'governorate': ?0, 'active': true}")
    List<CampingCenter> findActiveByGovernorate(String governorate);
    long countByGovernorate(String governorate);
}
