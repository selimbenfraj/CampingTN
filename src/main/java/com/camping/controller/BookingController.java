package com.camping.controller;

import com.camping.model.Booking;
import com.camping.service.CampingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final CampingService campingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking, Authentication auth) {
        booking.setUserEmail(auth.getName());
        return ResponseEntity.ok(campingService.createBooking(booking));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Booking>> getMyBookings(Authentication auth) {
        return ResponseEntity.ok(campingService.getAllBookings().stream()
                .filter(b -> auth.getName().equals(b.getUserEmail())).toList());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(campingService.getAllBookings());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Booking> updateStatus(@PathVariable String id, @RequestParam String status) {
        return ResponseEntity.ok(campingService.updateBookingStatus(id, status));
    }
}
