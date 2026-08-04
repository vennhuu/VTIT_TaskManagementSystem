package com.vennhuu.TaskManagementSystem.Service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectCacheService Unit Tests")
class ProjectCacheServiceTest {

    @Mock 
    private ProjectRepository projectRepository;

    @InjectMocks 
    private ProjectCacheService projectCacheService;

    private Project project;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setId(1L);
        owner.setFullName("Owner");

        project = new Project();
        project.setId(10L);
        project.setName("Test Project");
        project.setCreatedBy(owner);
    }

    @Test
    @DisplayName("Should return cached project when found")
    void shouldReturnCachedProject() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectCacheService.getProjectCached(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found")
    void shouldThrowWhenProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectCacheService.getProjectCached(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy dự án");
    }

    @Test
    @DisplayName("Should execute evict without error")
    void shouldExecuteEvict() {
        projectCacheService.evict(10L);
    }
}
