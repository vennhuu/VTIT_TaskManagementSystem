package com.vennhuu.TaskManagementSystem.Service.mapper;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;

public final class ProjectMapper {

    public static ProjectResponse toResponse(Project project) {
        ProjectResponse res = new ProjectResponse();

        res.setId(project.getId());
        res.setName(project.getName());
        res.setDescription(project.getDescription());
        res.setCreatedAt(project.getCreatedAt());
        res.setUpdatedAt(project.getUpdatedAt());

        ProjectResponse.Owner owner = new ProjectResponse.Owner();
        owner.setId(project.getCreatedBy().getId());
        owner.setFullName(project.getCreatedBy().getFullName());
        owner.setEmail(project.getCreatedBy().getEmail());

        res.setOwner(owner);

        return res;
    }
}
