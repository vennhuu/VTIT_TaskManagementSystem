package com.vennhuu.TaskManagementSystem.Service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.task.TaskReq;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.TaskRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Utils.SecurityUtil;
import com.vennhuu.TaskManagementSystem.Utils.constant.TaskStatus;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    private User getCurrentUser() {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("Không tìm thấy user");
        }

        return user;
    }

    public TaskResponse createTask(Long projectId, TaskReq req) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project không tồn tại"));

        User currentUser = getCurrentUser();

        ProjectMember currentMember =
                projectMemberRepository.findByProjectIdAndUserId(
                        projectId,
                        currentUser.getId());

        if (currentMember == null) {
            throw new RuntimeException("Bạn không thuộc project");
        }

        User assignee = userRepository.findById(req.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee không tồn tại"));

        boolean assigneeInProject =
                projectMemberRepository.existsByProjectIdAndUserId(
                        projectId,
                        assignee.getId());

        if (!assigneeInProject) {
            throw new RuntimeException("Assignee không thuộc project");
        }

        if (req.getDueDate() != null &&
                req.getDueDate().isBefore(Instant.now())) {

            throw new RuntimeException("DueDate không hợp lệ");
        }

        Task task = new Task();

        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());

        task.setProject(project);

        task.setCreatedBy(currentUser);

        task.setAssignee(assignee);

        task.setDueDate(req.getDueDate());

        task.setStatus(TaskStatus.TODO);

        Task saved = taskRepository.save(task);

        return convert(saved);
    }

    /**
     * Get all task
     */
    public List<TaskResponse> getAllTask(Long projectId) {

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::convert)
                .toList();
    }

    /**
     * Get detail
     */
    public TaskResponse getTask(Long projectId, Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task không thuộc project");
        }

        return convert(task);
    }

    /**
     * Update task
     */
    public TaskResponse updateTask(Long projectId, Long taskId, TaskReq req) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task không thuộc project");
        }

        User assignee = userRepository.findById(req.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee không tồn tại"));

        boolean exists =
                projectMemberRepository.existsByProjectIdAndUserId(
                        projectId,
                        assignee.getId());

        if (!exists) {
            throw new RuntimeException("Assignee không thuộc project");
        }

        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setDueDate(req.getDueDate());
        task.setAssignee(assignee);

        Task saved = taskRepository.save(task);

        return convert(saved);
    }

    /**
     * Update status
     */
    public TaskResponse updateStatus(
            Long projectId,
            Long taskId,
            TaskStatus status) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task không thuộc project");
        }

        task.setStatus(status);

        return convert(taskRepository.save(task));
    }

    /**
     * Delete task
     */
    public void deleteTask(Long projectId, Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task không thuộc project");
        }

        taskRepository.delete(task);
    }

    /**
     * Convert DTO
     */
    private TaskResponse convert(Task task) {

        TaskResponse res = new TaskResponse();

        res.setId(task.getId());
        res.setTitle(task.getTitle());
        res.setDescription(task.getDescription());

        res.setStatus(task.getStatus());

        res.setDueDate(task.getDueDate());

        res.setCreatedAt(task.getCreatedAt());

        res.setProjectId(task.getProject().getId());

        res.setCreatedById(task.getCreatedBy().getId());
        res.setCreatedByName(task.getCreatedBy().getFullName());

        res.setAssigneeId(task.getAssignee().getId());
        res.setAssigneeName(task.getAssignee().getFullName());

        return res;
    }

}