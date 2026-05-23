package com.camping.service;

import com.camping.model.*;
import com.camping.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CampingService {

    private final CampingCenterRepository campingRepo;
    private final BookingRepository bookingRepo;
    private final ReviewRepository reviewRepo;

    public List<CampingCenter> getAllCenters() {
        return campingRepo.findByActiveTrue();
    }

    public List<CampingCenter> getCentersByGovernorate(String gov) {
        return campingRepo.findActiveByGovernorate(gov);
    }

    public List<CampingCenter> getCentersBySiteType(String siteType) {
        return campingRepo.findBySiteNatureContainingIgnoreCase(siteType);
    }

    public Optional<CampingCenter> getCenterById(String id) {
        return campingRepo.findById(id);
    }

    public CampingCenter saveCenter(CampingCenter center) {
        return campingRepo.save(center);
    }

    public void deleteCenter(String id) {
        campingRepo.findById(id).ifPresent(c -> {
            c.setActive(false);
            campingRepo.save(c);
        });
    }

    // Regional statistics from dataset
    public Map<String, Object> getRegionalStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        List<CampingCenter> all = campingRepo.findAll();
        stats.put("totalCenters", all.size());
        stats.put("totalCapacity", all.stream().mapToInt(CampingCenter::getCapacityTotal).sum());

        Map<String, Long> byRegion = new LinkedHashMap<>();
        byRegion.put("Nord-Est", campingRepo.countByGovernorate("Bizerte") + campingRepo.countByGovernorate("Nabeul"));
        byRegion.put("Nord-Ouest", campingRepo.countByGovernorate("Jendouba") + campingRepo.countByGovernorate("Beja"));
        byRegion.put("District-Tunis", campingRepo.countByGovernorate("Manouba") + campingRepo.countByGovernorate("Tunis"));
        byRegion.put("Centre-Est", campingRepo.countByGovernorate("Sousse") + campingRepo.countByGovernorate("Sfax") + campingRepo.countByGovernorate("Mahdia"));
        byRegion.put("Centre-Ouest", campingRepo.countByGovernorate("Kasserine"));
        byRegion.put("Sud-Est", campingRepo.countByGovernorate("Médenine") + campingRepo.countByGovernorate("Tataouine"));
        byRegion.put("Sud-Ouest", campingRepo.countByGovernorate("Kébili"));
        stats.put("byRegion", byRegion);

        return stats;
    }

    // ---- Bookings ----
    public Booking createBooking(Booking booking) {
        booking.setStatus("PENDING");
        return bookingRepo.save(booking);
    }

    public List<Booking> getUserBookings(String userId) {
        return bookingRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    public Booking updateBookingStatus(String id, String status) {
        return bookingRepo.findById(id).map(b -> {
            b.setStatus(status);
            b.setUpdatedAt(LocalDateTime.now());
            return bookingRepo.save(b);
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    // ---- Reviews ----
    public Review addReview(Review review) {
        Review saved = reviewRepo.save(review);
        // Update camp rating
        campingRepo.findById(review.getCampingCenterId()).ifPresent(c -> {
            List<Review> reviews = reviewRepo.findByCampingCenterId(c.getId());
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            c.setRating(Math.round(avg * 10.0) / 10.0);
            c.setReviewCount(reviews.size());
            campingRepo.save(c);
        });
        return saved;
    }

    public List<Review> getCenterReviews(String centerId) {
        return reviewRepo.findByCampingCenterId(centerId);
    }

    // ---- Dashboard Stats ----
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCenters", campingRepo.count());
        stats.put("totalBookings", bookingRepo.count());
        stats.put("pendingBookings", bookingRepo.countByStatus("PENDING"));
        stats.put("confirmedBookings", bookingRepo.countByStatus("CONFIRMED"));
        double totalRevenue = bookingRepo.findAll().stream()
                .filter(b -> "COMPLETED".equals(b.getStatus()))
                .mapToDouble(Booking::getTotalCost).sum();
        stats.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        return stats;
    }
}
