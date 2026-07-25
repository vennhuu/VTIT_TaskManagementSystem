package com.vennhuu.TaskManagementSystem.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vennhuu.TaskManagementSystem.Entity.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    List<ActivityLog> findByTaskIdOrderByUpdatedAtDesc(Long taskId);

    List<ActivityLog> findByTaskProjectIdOrderByUpdatedAtDesc(Long projectId);
}
