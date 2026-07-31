package com.vennhuu.TaskManagementSystem.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.task.TaskReq;
import com.vennhuu.TaskManagementSystem.Entity.res.AssignTaskEmailMessage;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.task.TaskResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.TaskRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Service.cache.TaskCacheService;
import com.vennhuu.TaskManagementSystem.Service.mapper.TaskMapper;
import com.vennhuu.TaskManagementSystem.Service.producer.RabbitMQProducer;
import com.vennhuu.TaskManagementSystem.Spec.TaskSpecification;
import com.vennhuu.TaskManagementSystem.Utils.AuthContextHolder;
import com.vennhuu.TaskManagementSystem.Utils.constant.TaskStatus;
import com.vennhuu.TaskManagementSystem.Utils.errors.BadRequestException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ForbiddenException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogService activityLogService;
    private final TaskCacheService taskCacheService;
    private final RabbitMQProducer rabbitMQProducer;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            ActivityLogService activityLogService,
            TaskCacheService taskCacheService,
            RabbitMQProducer rabbitMQProducer
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.activityLogService = activityLogService;
        this.taskCacheService = taskCacheService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    public TaskResponse createTask(Long projectId, TaskReq req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project không tồn tại"));

        User currentUser = AuthContextHolder.getCurrentUser(userRepository);

        boolean currentUserInProject = projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!currentUserInProject) {
            throw new ForbiddenException("Bạn không thuộc project này");
        }

        User assignee = userRepository.findById(req.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Người được giao không tồn tại"));

        boolean assigneeInProject = projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId());
        if (!assigneeInProject) {
            throw new BadRequestException("Người được giao không thuộc project");
        }

        if (req.getDueDate() != null && req.getDueDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Ngày giao hạn không được ở trong quá khứ");
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

        String dueDateStr = saved.getDueDate() != null ? saved.getDueDate().toString() : "Chưa có hạn";
        AssignTaskEmailMessage message = new AssignTaskEmailMessage(
                assignee.getEmail(),
                assignee.getFullName(),
                saved.getTitle(),
                project.getName(),
                dueDateStr
        );
        rabbitMQProducer.sendAssignTaskEmail(message);

        return TaskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllTask(Long projectId, Specification<Task> spec, Pageable pageable) {
        Specification<Task> finalSpec = Specification.where(TaskSpecification.hasProject(projectId)).and(spec);
        Page<Task> page = taskRepository.findAll(finalSpec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setCurrentPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize());
        meta.setTotalPages(page.getTotalPages());
        meta.setTotalElements(page.getTotalElements());

        List<TaskResponse> tasks = page.getContent()
                .stream()
                .map(TaskMapper::toResponse)
                .toList();

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(tasks);
        return result;
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long projectId, Long taskId) {
        User currentUser = AuthContextHolder.getCurrentUser(userRepository);

        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!isMember) {
            throw new ForbiddenException("Quyền truy cập bị từ chối");
        }

        return taskCacheService.getTaskCached(projectId, taskId);
    }

    public TaskResponse updateTask(Long projectId, Long taskId, TaskReq req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Task không thuộc project này");
        }

        User assignee = userRepository.findById(req.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Người được giao không tồn tại"));

        boolean assigneeInProject = projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId());
        if (!assigneeInProject) {
            throw new BadRequestException("Người được giao không thuộc project");
        }

        if (req.getDueDate() != null && req.getDueDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Ngày giao hạn không được ở trong quá khứ");
        }

        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setDueDate(req.getDueDate());
        task.setAssignee(assignee);

        Task saved = taskRepository.save(task);

        taskCacheService.evict(projectId, taskId);

        return TaskMapper.toResponse(saved);
    }

    public TaskResponse updateStatus(Long projectId, Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Task không thuộc project này");
        }

        User currentUser = AuthContextHolder.getCurrentUser(userRepository);
        TaskStatus oldStatus = task.getStatus();

        task.setStatus(status);
        Task saved = taskRepository.save(task);

        activityLogService.save(saved, currentUser, oldStatus, status);
        taskCacheService.evict(projectId, taskId);

        return TaskMapper.toResponse(saved);
    }

    public void deleteTask(Long projectId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Task không thuộc project này");
        }

        taskCacheService.evict(projectId, taskId);
        taskRepository.delete(task);
    }
}
