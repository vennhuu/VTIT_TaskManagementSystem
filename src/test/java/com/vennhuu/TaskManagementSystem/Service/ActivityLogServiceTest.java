package com.vennhuu.TaskManagementSystem.Service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vennhuu.TaskManagementSystem.Entity.ActivityLog;
import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.res.activityLog.ActivityLogResponse;
import com.vennhuu.TaskManagementSystem.Repository.ActivityLogRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Utils.AuthContextHolder;
import com.vennhuu.TaskManagementSystem.Utils.constant.TaskStatus;
import com.vennhuu.TaskManagementSystem.Utils.errors.ForbiddenException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityLogService Unit Tests")
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    private User user;
    private Project project;
    private Task task;
    private ActivityLog log;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("User");
        user.setEmail("user@test.com");

        project = new Project();
        project.setId(10L);
        project.setName("Project");

        task = new Task();
        task.setId(100L);
        task.setTitle("Task");
        task.setProject(project);
        task.setCreatedBy(user);
        task.setAssignee(user);

        log = new ActivityLog();
        log.setId(1L);
        log.setTask(task);
        log.setUser(user);
        log.setFromStatus(TaskStatus.TODO);
        log.setToStatus(TaskStatus.IN_PROGRESS);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should save activity log with correct data")
        void shouldSaveActivityLog() {
            when(activityLogRepository.save(any())).thenReturn(log);

            activityLogService.save(task, user, TaskStatus.TODO, TaskStatus.IN_PROGRESS);

            verify(activityLogRepository).save(argThat(l ->
                    l.getTask().equals(task)
                    && l.getUser().equals(user)
                    && l.getFromStatus() == TaskStatus.TODO
                    && l.getToStatus() == TaskStatus.IN_PROGRESS
            ));
        }
    }

    @Nested
    @DisplayName("getByTask")
    class GetByTask {

        @Test
        @DisplayName("Should return activity logs for given task")
        void shouldReturnLogsForTask() {
            when(activityLogRepository.findByTaskIdOrderByUpdatedAtDesc(100L)).thenReturn(List.of(log));

            List<ActivityLogResponse> result = activityLogService.getByTask(100L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTaskId()).isEqualTo(100L);
            assertThat(result.get(0).getFromStatus()).isEqualTo(TaskStatus.TODO);
            assertThat(result.get(0).getToStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should return empty list when no logs found")
        void shouldReturnEmptyList() {
            when(activityLogRepository.findByTaskIdOrderByUpdatedAtDesc(100L)).thenReturn(List.of());

            List<ActivityLogResponse> result = activityLogService.getByTask(100L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllLogByProject()")
    class GetAllLogByProject {

        @Test
        @DisplayName("Should return all logs when user is member")
        void shouldReturnAllLogsWhenMember() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(true);
            when(activityLogRepository.findByTaskProjectIdOrderByUpdatedAtDesc(10L)).thenReturn(List.of(log));

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(user);

                List<ActivityLogResponse> result = activityLogService.getAllLogByProject(10L);

                assertThat(result).hasSize(1);
                assertThat(result.get(0).getUserId()).isEqualTo(1L);
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when project not found")
        void shouldThrowWhenProjectNotFound() {
            when(projectRepository.findById(99L)).thenReturn(Optional.empty());

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(user);

                assertThatThrownBy(() -> activityLogService.getAllLogByProject(99L))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Project không tồn tại");
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user is not member")
        void shouldThrowWhenNotMember() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(false);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(user);

                assertThatThrownBy(() -> activityLogService.getAllLogByProject(10L))
                        .isInstanceOf(ForbiddenException.class)
                        .hasMessageContaining("không thuộc project");
            }
        }
    }
}
