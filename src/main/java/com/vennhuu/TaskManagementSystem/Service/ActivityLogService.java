package com.vennhuu.TaskManagementSystem.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vennhuu.TaskManagementSystem.Entity.ActivityLog;
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

@Service
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository
    ) {
        this.activityLogRepository = activityLogRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public void save(Task task, User user, TaskStatus from, TaskStatus to) {
        ActivityLog log = new ActivityLog();
        log.setTask(task);
        log.setUser(user);
        log.setFromStatus(from);
        log.setToStatus(to);
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getByTask(Long taskId) {
        return activityLogRepository.findByTaskIdOrderByUpdatedAtDesc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getAllLogByProject(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project không tồn tại"));

        User currentUser = AuthContextHolder.getCurrentUser(userRepository);

        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!isMember) {
            throw new ForbiddenException("Bạn không thuộc project này");
        }

        return activityLogRepository.findByTaskProjectIdOrderByUpdatedAtDesc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        ActivityLogResponse res = new ActivityLogResponse();
        res.setId(log.getId());
        res.setTaskId(log.getTask().getId());
        res.setUserId(log.getUser().getId());
        res.setFullName(log.getUser().getFullName());
        res.setFromStatus(log.getFromStatus());
        res.setToStatus(log.getToStatus());
        res.setUpdatedAt(log.getUpdatedAt());
        return res;
    }
}
