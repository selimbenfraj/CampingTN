package com.camping.controller;

import com.camping.dto.AdminUserDTO;
import com.camping.model.*;
import com.camping.service.AdminService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<AdminUserDTO.UserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDTO.UserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    @PostMapping("/users")
    public ResponseEntity<AdminUserDTO.UserResponse> createUser(@Valid @RequestBody AdminUserDTO.UserRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<AdminUserDTO.UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody AdminUserDTO.UserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserDTO.UserResponse> updateUserStatus(@PathVariable String id, @RequestParam boolean active) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, active));
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<AdminUserDTO.UserResponse> promoteUser(@PathVariable String id) {
        return ResponseEntity.ok(adminService.promoteToAdmin(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
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
