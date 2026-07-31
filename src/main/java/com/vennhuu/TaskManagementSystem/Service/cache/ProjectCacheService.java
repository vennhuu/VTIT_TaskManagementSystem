package com.vennhuu.TaskManagementSystem.Service.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Service.mapper.ProjectMapper;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@Service
public class ProjectCacheService {

    private final ProjectRepository projectRepository;

    public ProjectCacheService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getProjectCached(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án"));
        return ProjectMapper.toResponse(project);
    }

    @CacheEvict(value = "projects", key = "#id")
    public void evict(Long id) {
    }
}
