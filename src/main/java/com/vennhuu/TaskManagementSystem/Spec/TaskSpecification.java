package com.vennhuu.TaskManagementSystem.Spec;

import org.springframework.data.jpa.domain.Specification;

import com.vennhuu.TaskManagementSystem.Entity.Task;

public class TaskSpecification {
    
    public static Specification<Task> hasProject(Long projectId) {
        return (root, query, cb) ->
                cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, cb) ->
                cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task> createdBy(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("createdBy").get("id"), userId);
    }
}
