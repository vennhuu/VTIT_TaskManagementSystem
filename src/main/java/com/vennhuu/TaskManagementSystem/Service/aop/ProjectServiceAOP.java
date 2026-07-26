package com.vennhuu.TaskManagementSystem.Service.aop;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;

@Service
public class ProjectServiceAOP {

    private final ProjectRepository projectRepository ;

    public ProjectServiceAOP(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    private ProjectResponse toResponse(Project project) {

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

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getProjectCached(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dự án này"));
        return toResponse(project);
    }
}
