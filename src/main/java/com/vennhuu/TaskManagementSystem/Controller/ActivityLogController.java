package com.vennhuu.TaskManagementSystem.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vennhuu.TaskManagementSystem.Entity.res.activityLog.ActivityLogResponse;
import com.vennhuu.TaskManagementSystem.Service.ActivityLogService;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;


@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/tasks/{taskId}/activities")
    @APIMessage("Get activity logs by task")
    public ResponseEntity<List<ActivityLogResponse>> getLogsByTask(@PathVariable Long projectId, @PathVariable Long taskId) {

        return ResponseEntity.ok(activityLogService.getByTask(taskId));
    }

    @GetMapping("/activities")
    @APIMessage("Get all activity logs")
    public ResponseEntity<List<ActivityLogResponse>> getAllLogs(@PathVariable Long projectId) {

        return ResponseEntity.ok(activityLogService.getAllLogByProject(projectId));
    }

    
}
