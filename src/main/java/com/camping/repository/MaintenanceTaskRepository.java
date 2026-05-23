package com.camping.repository;

import com.camping.model.MaintenanceTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MaintenanceTaskRepository extends MongoRepository<MaintenanceTask, String> {
    List<MaintenanceTask> findByStatus(String status);
    List<MaintenanceTask> findByPriority(String priority);
    List<MaintenanceTask> findByAssignedTo(String assignedTo);
    long countByStatus(String status);
}
