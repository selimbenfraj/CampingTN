package com.camping.controller;

import com.camping.model.*;
import com.camping.service.CampingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/camping-centers")
@RequiredArgsConstructor
public class CampingController {

    private final CampingService campingService;

    @GetMapping
    public ResponseEntity<List<CampingCenter>> getAllCenters() {
        return ResponseEntity.ok(campingService.getAllCenters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampingCenter> getCenter(@PathVariable String id) {
        return campingService.getCenterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/governorate/{gov}")
    public ResponseEntity<List<CampingCenter>> getCentersByGov(@PathVariable String gov) {
        return ResponseEntity.ok(campingService.getCentersByGovernorate(gov));
    }

    @GetMapping("/type/{siteType}")
    public ResponseEntity<List<CampingCenter>> getCentersByType(@PathVariable String siteType) {
        return ResponseEntity.ok(campingService.getCentersBySiteType(siteType));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(campingService.getRegionalStats());
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable String id) {
        return ResponseEntity.ok(campingService.getCenterReviews(id));
    }

    @PostMapping("/{id}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Review> addReview(@PathVariable String id, @RequestBody Review review) {
        review.setCampingCenterId(id);
        return ResponseEntity.ok(campingService.addReview(review));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampingCenter> createCenter(@RequestBody CampingCenter center) {
        return ResponseEntity.ok(campingService.saveCenter(center));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampingCenter> updateCenter(@PathVariable String id, @RequestBody CampingCenter center) {
        center.setId(id);
        return ResponseEntity.ok(campingService.saveCenter(center));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCenter(@PathVariable String id) {
        campingService.deleteCenter(id);
        return ResponseEntity.noContent().build();
    }
}
