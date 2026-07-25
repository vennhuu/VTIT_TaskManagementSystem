package com.vennhuu.TaskManagementSystem.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.ActivityLog;
import com.vennhuu.TaskManagementSystem.Entity.Task;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.res.activityLog.ActivityLogResponse;
import com.vennhuu.TaskManagementSystem.Repository.ActivityLogRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Utils.SecurityUtil;
import com.vennhuu.TaskManagementSystem.Utils.constant.TaskStatus;

@Service
public class ActivityLogService {
    
    private final ActivityLogRepository activityLogRepository ;
    private final ProjectRepository projectRepository ;
    private final UserRepository userRepository ;
    private final ProjectMemberRepository projectMemberRepository;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository, 
            ProjectRepository projectRepository, 
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository
        ) {
        this.activityLogRepository = activityLogRepository;
        this.projectRepository = projectRepository ;
        this.userRepository = userRepository ;
        this.projectMemberRepository = projectMemberRepository ;
    }

    private User getCurrentUser() {

        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        return userRepository.findByEmail(email);
    }

    public void save(Task task, User user, TaskStatus from, TaskStatus to) {

        ActivityLog log = new ActivityLog();

        log.setTask(task);
        log.setUser(user);
        log.setFromStatus(from);
        log.setToStatus(to);

        activityLogRepository.save(log);
    }

    public List<ActivityLogResponse> getByTask(Long taskId) {

        return activityLogRepository.findByTaskIdOrderByUpdatedAtDesc(taskId)
                .stream()
                .map(this::convert)
                .toList();
    }

    public List<ActivityLogResponse> getAllLogByProject(Long projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project không tồn tại"));

        // Lấy user hiện tại
        User currentUser = getCurrentUser();

        // Kiểm tra user có thuộc project không
        boolean exists = projectMemberRepository.existsByProjectIdAndUserId(
                projectId,
                currentUser.getId());
        
        if (!exists) {
            throw new RuntimeException("Bạn không thuộc project này");
        }
        return activityLogRepository.findByTaskProjectIdOrderByUpdatedAtDesc(projectId)
                .stream()
                .map(this::convert)
                .toList();
    }

    private ActivityLogResponse convert(ActivityLog log) {

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
