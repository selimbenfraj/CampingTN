package com.camping.repository;

import com.camping.model.Review;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByCampingCenterId(String campingCenterId);

    List<Review> findByUserId(String userId);

    @Aggregation(pipeline = {
        "{ '$match': { 'campingCenterId': ?0 } }",
        "{ '$group': { '_id': null, 'avgRating': { '$avg': '$rating' } } }",
        "{ '$project': { '_id': 0, 'avgRating': 1 } }"
    })
    Double avgRatingByCampingCenterId(String campingCenterId);
}