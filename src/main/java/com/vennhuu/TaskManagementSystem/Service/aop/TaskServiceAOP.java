package com.vennhuu.TaskManagementSystem.Service.aop;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Repository.TaskRepository;

@Service
public class TaskServiceAOP {

    private final TaskRepository taskRepository;

    public TaskServiceAOP(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // key ghép cả projectId + taskId vì method cần cả 2 tham số để validate
    @Cacheable(value = "tasks", key = "#projectId + '-' + #taskId")
    public TaskResponse getTaskCached(Long projectId, Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task không thuộc project");
        }

        return convert(task);
    }

    private TaskResponse convert(Task task) {
        TaskResponse res = new TaskResponse();
        res.setId(task.getId());
        res.setTitle(task.getTitle());
        res.setDescription(task.getDescription());
        res.setStatus(task.getStatus());
        res.setDueDate(task.getDueDate());
        res.setCreatedAt(task.getCreatedAt());
        res.setProjectId(task.getProject().getId());
        res.setCreatedByName(task.getCreatedBy().getFullName());
        res.setAssigneeId(task.getAssignee().getId());
        res.setAssigneeName(task.getAssignee().getFullName());
        return res;
    }
}