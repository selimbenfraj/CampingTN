package com.camping.repository;

import com.camping.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId);
    List<Booking> findByCampingCenterId(String campingCenterId);
    List<Booking> findByStatus(String status);
    long countByStatus(String status);
    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);
}
