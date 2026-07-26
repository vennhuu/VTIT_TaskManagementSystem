package com.vennhuu.TaskManagementSystem.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.task.TaskReq;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.TaskRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Service.aop.TaskServiceAOP;
import com.vennhuu.TaskManagementSystem.Spec.TaskSpecification;
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
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final TaskServiceAOP taskServiceAOP;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            ActivityLogService activityLogService,
            NotificationService notificationService,
            TaskServiceAOP taskServiceAOP
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
        this.taskServiceAOP = taskServiceAOP;
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

        Project project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project không tồn tại"));

        User currentUser = getCurrentUser();

        ProjectMember currentMember = this.projectMemberRepository.findByProjectIdAndUserId(projectId, currentUser.getId());

        if (currentMember == null) {
            throw new RuntimeException("Bạn không thuộc project");
        }

        User assignee = this.userRepository.findById(req.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Người được giao không tồn tại"));

        boolean assigneeInProject = this.projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId());

        if (!assigneeInProject) {
            throw new RuntimeException("Người được giao không thuộc project");
        }

        if (req.getDueDate() != null && req.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày giao hạn không hợp lệ");
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

        notificationService.notifyAssignTask(saved);

        return convert(saved);
    }

    public ResultPaginationDTO getAllTask(Long projectId, Specification<Task> spec, Pageable pageable) {

        Specification<Task> finalSpec = Specification.where(TaskSpecification.hasProject(projectId)).and(spec);

        Page<Task> pageTask = taskRepository.findAll(finalSpec, pageable);

        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setCurrentPage(pageTask.getNumber() + 1);
        meta.setPageSize(pageTask.getSize());
        meta.setTotalPages(pageTask.getTotalPages());
        meta.setTotalElements(pageTask.getTotalElements());

        res.setMeta(meta);
        List<TaskResponse> listTask = pageTask.getContent()
                .stream()
                .map(this::convert)
                .toList();

        res.setResult(listTask);
        return res;
    }

    public TaskResponse getTask(Long projectId, Long taskId) {

        User currentUser = getCurrentUser();

        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!isMember) {
            throw new RuntimeException("Quyền truy cập bị từ chối");
        }

        return this.taskServiceAOP.getTaskCached(projectId, taskId); // đổi tên rõ nghĩa hơn
    }

    public TaskResponse updateTask(Long projectId, Long taskId, TaskReq req) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task không thuộc project");
        }

        User assignee = userRepository.findById(req.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Người được giao không tồn tại"));

        boolean exists = projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId());

        if (!exists) {
            throw new RuntimeException("Người được giao không thuộc project");
        }

        if (req.getDueDate() != null && req.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày giao hạn không hợp lệ");
        }

        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setDueDate(req.getDueDate());
        task.setAssignee(assignee);

        Task saved = taskRepository.save(task);

        return convert(saved);
    }

    public TaskResponse updateStatus(Long projectId, Long taskId, TaskStatus status) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task không thuộc project");
        }

        User currentUser = getCurrentUser();

        TaskStatus oldStatus = task.getStatus();

        task.setStatus(status);

        Task saved = taskRepository.save(task);

        activityLogService.save(saved, currentUser, oldStatus, status);

        return convert(saved);
    }

    public void deleteTask(Long projectId, Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task không thuộc project");
        }

        taskRepository.delete(task);
    }

    private TaskResponse convert(Task task) {

        TaskResponse res = new TaskResponse();

        res.setId(task.getId());
        res.setTitle(task.getTitle());
        res.setDescription(task.getDescription());
        res.setStatus(task.getStatus());
        res.setDueDate(task.getDueDate());
        res.setCreatedAt(task.getCreatedAt());
        res.setProjectId(task.getProject().getId());
        // res.setCreatedById(task.getCreatedBy().getId());
        res.setCreatedByName(task.getCreatedBy().getFullName());
        res.setAssigneeId(task.getAssignee().getId());
        res.setAssigneeName(task.getAssignee().getFullName());

        return res;
    }

}
