package com.vennhuu.TaskManagementSystem.Controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.req.task.TaskReq;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Entity.res.task.UpdateStatus;
import com.vennhuu.TaskManagementSystem.Service.TaskService;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @APIMessage("Create task")
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long projectId, @Valid @RequestBody TaskReq req) {

        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, req));
    }

    @GetMapping
    @APIMessage("Get all task")
    public ResponseEntity<ResultPaginationDTO> getAllTask(
            @PathVariable Long projectId,
            @Filter Specification<Task> spec,
            Pageable pageable ) {

        return ResponseEntity.ok(taskService.getAllTask(projectId, spec, pageable));
    }

    @GetMapping("/{taskId}")
    @APIMessage("Get task detail")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {

        return ResponseEntity.ok(taskService.getTask(projectId, taskId));
    }

    @PutMapping("/{taskId}")
    @APIMessage("Update task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskReq req) {

        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, req));
    }

    @PatchMapping("/{taskId}/status")
    @APIMessage("Update task status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody UpdateStatus status) {

        return ResponseEntity.ok(taskService.updateStatus(projectId, taskId, status));
    }

    @DeleteMapping("/{taskId}")
    @APIMessage("Delete task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {

        taskService.deleteTask(projectId, taskId);

        return ResponseEntity.noContent().build();
    }

}