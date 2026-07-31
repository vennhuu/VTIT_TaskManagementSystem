package com.vennhuu.TaskManagementSystem.Service.mapper;

import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;

public final class TaskMapper {

    private TaskMapper() {}

    public static TaskResponse toResponse(Task task) {
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
