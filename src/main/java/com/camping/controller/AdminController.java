package com.camping.controller;

import com.camping.model.*;
import com.camping.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getFullDashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable String id, @RequestParam boolean active) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, active));
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<User> promoteUser(@PathVariable String id) {
        return ResponseEntity.ok(adminService.promoteToAdmin(id));
    }

    @GetMapping("/activity")
    public ResponseEntity<List<Map<String, Object>>> getActivity() {
        return ResponseEntity.ok(adminService.getRecentActivity());
    }

    @GetMapping("/maintenance")
    public ResponseEntity<List<MaintenanceTask>> getTasks() {
        return ResponseEntity.ok(adminService.getAllTasks());
    }

    @PostMapping("/maintenance")
    public ResponseEntity<MaintenanceTask> createTask(@RequestBody MaintenanceTask task) {
        return ResponseEntity.ok(adminService.createTask(task));
    }

    @PutMapping("/maintenance/{id}")
    public ResponseEntity<MaintenanceTask> updateTask(@PathVariable String id, @RequestBody MaintenanceTask task) {
        return ResponseEntity.ok(adminService.updateTask(id, task));
    }

    @DeleteMapping("/maintenance/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        adminService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
