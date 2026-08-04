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
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Repository.TaskRepository;
import com.vennhuu.TaskManagementSystem.Utils.errors.BadRequestException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskCacheService Unit Tests")
class TaskCacheServiceTest {

    @Mock 
    private TaskRepository taskRepository;

    @InjectMocks 
    
    private TaskCacheService taskCacheService;

    private Task task;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setFullName("User");

        Project project = new Project();
        project.setId(10L);

        task = new Task();
        task.setId(100L);
        task.setProject(project);
        task.setCreatedBy(user);
        task.setAssignee(user);
    }

    @Test
    @DisplayName("Should return cached task when task exists in project")
    void shouldReturnCachedTask() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskResponse response = taskCacheService.getTaskCached(10L, 100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task not found")
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCacheService.getTaskCached(10L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task không tồn tại");
    }

    @Test
    @DisplayName("Should throw BadRequestException when task belongs to different project")
    void shouldThrowWhenTaskNotInProject() {
        Project otherProject = new Project();
        otherProject.setId(99L);
        task.setProject(otherProject);

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskCacheService.getTaskCached(10L, 100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Task không thuộc project này");
    }

    @Test
    @DisplayName("Should execute evict without error")
    void shouldExecuteEvict() {
        taskCacheService.evict(10L, 100L);
    }
}
