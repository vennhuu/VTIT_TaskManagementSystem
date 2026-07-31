package com.vennhuu.TaskManagementSystem.Service;

import java.time.LocalDate;
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
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.task.TaskReq;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.TaskRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Service.cache.TaskCacheService;
import com.vennhuu.TaskManagementSystem.Service.producer.RabbitMQProducer;
import com.vennhuu.TaskManagementSystem.Utils.AuthContextHolder;
import com.vennhuu.TaskManagementSystem.Utils.constant.TaskStatus;
import com.vennhuu.TaskManagementSystem.Utils.errors.BadRequestException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ForbiddenException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private TaskCacheService taskCacheService;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @InjectMocks
    private TaskService taskService;

    private User creator;
    private User assignee;
    private Project project;
    private Task task;
    private TaskReq taskReq;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setFullName("Creator");
        creator.setEmail("creator@test.com");

        assignee = new User();
        assignee.setId(2L);
        assignee.setFullName("Assignee");
        assignee.setEmail("assignee@test.com");

        project = new Project();
        project.setId(10L);
        project.setName("Test Project");
        project.setCreatedBy(creator);

        task = new Task();
        task.setId(100L);
        task.setTitle("Test Task");
        task.setDescription("Task desc");
        task.setProject(project);
        task.setCreatedBy(creator);
        task.setAssignee(assignee);
        task.setStatus(TaskStatus.TODO);

        taskReq = new TaskReq();
        taskReq.setTitle("Test Task");
        taskReq.setDescription("Task desc");
        taskReq.setAssigneeId(2L);
    }

    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("Should create task successfully")
        void shouldCreateTaskSuccessfully() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(true);
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);
            when(taskRepository.save(any())).thenReturn(task);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                TaskResponse response = taskService.createTask(10L, taskReq);

                assertThat(response).isNotNull();
                assertThat(response.getTitle()).isEqualTo("Test Task");
                verify(rabbitMQProducer).sendAssignTaskEmail(any());
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when project not found")
        void shouldThrowWhenProjectNotFound() {
            when(projectRepository.findById(99L)).thenReturn(Optional.empty());

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                assertThatThrownBy(() -> taskService.createTask(99L, taskReq))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Project không tồn tại");
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when creator not in project")
        void shouldThrowWhenCreatorNotInProject() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(false);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                assertThatThrownBy(() -> taskService.createTask(10L, taskReq))
                        .isInstanceOf(ForbiddenException.class)
                        .hasMessageContaining("không thuộc project");
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when assignee not found")
        void shouldThrowWhenAssigneeNotFound() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(true);
            when(userRepository.findById(2L)).thenReturn(Optional.empty());

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                assertThatThrownBy(() -> taskService.createTask(10L, taskReq))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Người được giao không tồn tại");
            }
        }

        @Test
        @DisplayName("Should throw BadRequestException when assignee not in project")
        void shouldThrowWhenAssigneeNotInProject() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(true);
            when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(false);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                assertThatThrownBy(() -> taskService.createTask(10L, taskReq))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("Người được giao không thuộc project");
            }
        }

        @Test
        @DisplayName("Should throw BadRequestException when dueDate is in the past")
        void shouldThrowWhenDueDateInPast() {
            taskReq.setDueDate(LocalDate.now().minusDays(1));

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(true);
            when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                assertThatThrownBy(() -> taskService.createTask(10L, taskReq))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("quá khứ");
            }
        }
    }

    @Nested
    @DisplayName("getAllTask")
    class GetAllTask {

        @Test
        @DisplayName("Should return paginated task list")
        void shouldReturnPaginatedTasks() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);

            when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            ResultPaginationDTO result = taskService.getAllTask(10L, mock(Specification.class), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getMeta().getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getTask")
    class GetTask {

        @Test
        @DisplayName("Should return task detail from cache when user is member")
        void shouldReturnTaskWhenMember() {
            TaskResponse cached = new TaskResponse();
            cached.setId(100L);

            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(true);
            when(taskCacheService.getTaskCached(10L, 100L)).thenReturn(cached);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                TaskResponse result = taskService.getTask(10L, 100L);

                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(100L);
                verify(taskCacheService).getTaskCached(10L, 100L);
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user not member")
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(false);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                assertThatThrownBy(() -> taskService.getTask(10L, 100L))
                        .isInstanceOf(ForbiddenException.class)
                        .hasMessageContaining("Quyền truy cập bị từ chối");
            }
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTask {

        @Test
        @DisplayName("Should update task successfully")
        void shouldUpdateTaskSuccessfully() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);
            when(taskRepository.save(any())).thenReturn(task);

            TaskResponse result = taskService.updateTask(10L, 100L, taskReq);

            assertThat(result).isNotNull();
            verify(taskCacheService).evict(10L, 100L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when task not found")
        void shouldThrowWhenTaskNotFound() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(10L, 999L, taskReq))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Task không tồn tại");
        }

        @Test
        @DisplayName("Should throw BadRequestException when task not in project")
        void shouldThrowWhenTaskNotInProject() {
            Project otherProject = new Project();
            otherProject.setId(99L);
            task.setProject(otherProject);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> taskService.updateTask(10L, 100L, taskReq))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Task không thuộc project này");
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("Should update task status and log activity")
        void shouldUpdateStatusSuccessfully() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any())).thenReturn(task);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                TaskResponse result = taskService.updateStatus(10L, 100L, TaskStatus.IN_PROGRESS);

                assertThat(result).isNotNull();
                verify(activityLogService).save(any(), eq(creator), eq(TaskStatus.TODO), eq(TaskStatus.IN_PROGRESS));
                verify(taskCacheService).evict(10L, 100L);
            }
        }

        @Test
        @DisplayName("Should throw BadRequestException when task not in project")
        void shouldThrowWhenTaskNotInProject() {
            Project other = new Project();
            other.setId(99L);
            task.setProject(other);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(creator);

                assertThatThrownBy(() -> taskService.updateStatus(10L, 100L, TaskStatus.DONE))
                        .isInstanceOf(BadRequestException.class);
            }
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("Should delete task and evict cache")
        void shouldDeleteTaskSuccessfully() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            taskService.deleteTask(10L, 100L);

            verify(taskCacheService).evict(10L, 100L);
            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when task not found")
        void shouldThrowWhenTaskNotFound() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deleteTask(10L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw BadRequestException when task not in given project")
        void shouldThrowWhenTaskNotInProject() {
            Project other = new Project();
            other.setId(99L);
            task.setProject(other);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> taskService.deleteTask(10L, 100L))
                    .isInstanceOf(BadRequestException.class);
        }
    }
}
