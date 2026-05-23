package com.camping.service;

import com.camping.model.*;
import com.camping.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepo;
    private final CampingCenterRepository campingRepo;
    private final BookingRepository bookingRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final BudgetPredictionRepository predictionRepo;
    private final MaintenanceTaskRepository maintenanceRepo;

    public Map<String, Object> getFullDashboard() {
        Map<String, Object> dash = new LinkedHashMap<>();

        // Users
        dash.put("totalUsers", userRepo.count());

        // Camping
        dash.put("totalCampingCenters", campingRepo.count());
        long totalCapacity = campingRepo.findAll().stream()
                .mapToLong(CampingCenter::getCapacityTotal).sum();
        dash.put("totalCapacity", totalCapacity);

        // Bookings
        dash.put("totalBookings", bookingRepo.count());
        dash.put("pendingBookings", bookingRepo.countByStatus("PENDING"));
        dash.put("confirmedBookings", bookingRepo.countByStatus("CONFIRMED"));

        // Revenue from bookings
        double bookingRevenue = bookingRepo.findAll().stream()
                .filter(b -> "COMPLETED".equals(b.getStatus()))
                .mapToDouble(Booking::getTotalCost).sum();
        dash.put("bookingRevenue", Math.round(bookingRevenue * 100.0) / 100.0);

        // Shop
        dash.put("totalProducts", productRepo.count());
        dash.put("totalOrders", orderRepo.count());
        dash.put("pendingOrders", orderRepo.countByStatus("PENDING"));
        double orderRevenue = orderRepo.findAll().stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount).sum();
        dash.put("orderRevenue", Math.round(orderRevenue * 100.0) / 100.0);

        // Predictions
        dash.put("totalPredictions", predictionRepo.count());

        // Maintenance
        dash.put("openTasks", maintenanceRepo.countByStatus("OPEN"));
        dash.put("criticalTasks", maintenanceRepo.countByStatus("IN_PROGRESS"));

        // Total revenue
        dash.put("totalRevenue", Math.round((bookingRevenue + orderRevenue) * 100.0) / 100.0);

        return dash;
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User updateUserStatus(String id, boolean active) {
        return userRepo.findById(id).map(u -> {
            u.setActive(active);
            return userRepo.save(u);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User promoteToAdmin(String id) {
        return userRepo.findById(id).map(u -> {
            u.getRoles().add("ADMIN");
            return userRepo.save(u);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Maintenance Tasks
    public List<MaintenanceTask> getAllTasks() {
        return maintenanceRepo.findAll();
    }

    public MaintenanceTask createTask(MaintenanceTask task) {
        return maintenanceRepo.save(task);
    }

    public MaintenanceTask updateTask(String id, MaintenanceTask updated) {
        return maintenanceRepo.findById(id).map(t -> {
            t.setStatus(updated.getStatus());
            t.setPriority(updated.getPriority());
            t.setResolution(updated.getResolution());
            t.setNotes(updated.getNotes());
            t.setAssignedTo(updated.getAssignedTo());
            t.setUpdatedAt(LocalDateTime.now());
            return maintenanceRepo.save(t);
        }).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public void deleteTask(String id) {
        maintenanceRepo.deleteById(id);
    }

    public List<Map<String, Object>> getRecentActivity() {
        List<Map<String, Object>> activity = new ArrayList<>();

        bookingRepo.findAll().stream()
                .sorted(Comparator.comparing(b -> b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
                .limit(5)
                .forEach(b -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("type", "BOOKING");
                    m.put("icon", "🏕️");
                    m.put("description", "New booking at " + b.getCampingCenterName());
                    m.put("user", b.getUserEmail());
                    m.put("time", b.getCreatedAt());
                    activity.add(m);
                });

        orderRepo.findAll().stream()
                .sorted(Comparator.comparing(o -> o.getCreatedAt() != null ? o.getCreatedAt() : LocalDateTime.MIN, Comparator.reverseOrder()))
                .limit(5)
                .forEach(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("type", "ORDER");
                    m.put("icon", "🛒");
                    m.put("description", "New order — " + o.getTotalAmount() + " TND");
                    m.put("user", o.getUserEmail());
                    m.put("time", o.getCreatedAt());
                    activity.add(m);
                });

        activity.sort((a, b) -> {
            LocalDateTime ta = (LocalDateTime) a.get("time");
            LocalDateTime tb = (LocalDateTime) b.get("time");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });

        return activity.stream().limit(10).toList();
    }
}
