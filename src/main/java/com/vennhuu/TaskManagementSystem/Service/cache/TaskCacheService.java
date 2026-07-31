package com.vennhuu.TaskManagementSystem.Service.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Repository.TaskRepository;
import com.vennhuu.TaskManagementSystem.Service.mapper.TaskMapper;
import com.vennhuu.TaskManagementSystem.Utils.errors.BadRequestException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@Service
public class TaskCacheService {

    private final TaskRepository taskRepository;

    public TaskCacheService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Cacheable(value = "tasks", key = "#projectId + '-' + #taskId")
    public TaskResponse getTaskCached(Long projectId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Task không thuộc project này");
        }

        return TaskMapper.toResponse(task);
    }

    @CacheEvict(value = "tasks", key = "#projectId + '-' + #taskId")
    public void evict(Long projectId, Long taskId) {
    }
}
