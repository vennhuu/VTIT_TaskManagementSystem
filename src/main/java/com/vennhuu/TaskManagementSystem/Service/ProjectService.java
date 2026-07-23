package com.vennhuu.TaskManagementSystem.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.project.ProjectReq;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Utils.SecurityUtil;
import com.vennhuu.TaskManagementSystem.Utils.constant.ProjectRole;

@Service
public class ProjectService {
    
    private final ProjectRepository projectRepository ;
    private final UserRepository userRepository ;
    private final ProjectMemberRepository projectMemberRepository ;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository ;
        this.projectMemberRepository = projectMemberRepository ;
    }

    private User getCurrentUser() {

        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        return userRepository.findByEmail(email);
    }

    public List<ProjectResponse> getMyProjects() {

        User currentUser = getCurrentUser();

        List<Project> projects = this.projectRepository.findDistinctByMembersUserId(currentUser.getId());

        return projects.stream().map(this::toResponse).toList();
    }

    public Project save(Project project) {
        return this.projectRepository.save(project) ;
    }

    public ProjectResponse createProject( ProjectReq request) {

        User currentUser = getCurrentUser();

        Project project = new Project();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedBy(currentUser);

        this.projectRepository.save(project);

        ProjectMember member = new ProjectMember();

        member.setProject(project);
        member.setUser(currentUser);
        member.setRole(ProjectRole.OWNER);

        this.projectMemberRepository.save(member);

        return toResponse(project);
    }

    public ProjectResponse updateProject(Long id, ProjectReq request) {

        User currentUser = getCurrentUser();

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId( id, currentUser.getId() );

        if (member == null || member.getRole() != ProjectRole.OWNER) {
            throw new RuntimeException("Chỉ có chủ sở hữu mới được phép cập nhật");
        }

        Project project = member.getProject();

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        projectRepository.save(project);

        return toResponse(project);
    }


    public void deleteProject(Long id) {

        User currentUser = getCurrentUser();

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId( id, currentUser.getId() );

        if (member == null || member.getRole() != ProjectRole.OWNER) {
            throw new RuntimeException("Chỉ có chủ sở hữu mới được phép xóa");
        }

        this.projectRepository.deleteById(id);
    }

    private ProjectResponse toResponse(Project project) {

        ProjectResponse res = new ProjectResponse();

        res.setId(project.getId());
        res.setName(project.getName());
        res.setDescription(project.getDescription());
        res.setCreatedAt(project.getCreatedAt());
        res.setUpdatedAt(project.getUpdatedAt());

        ProjectResponse.Owner owner = new ProjectResponse.Owner();

        owner.setId(project.getCreatedBy().getId());
        owner.setFullName(project.getCreatedBy().getFullName());
        owner.setEmail(project.getCreatedBy().getEmail());

        res.setOwner(owner);

        return res;
    }

    public ProjectResponse getProject(Long id) {

        User currentUser = getCurrentUser();

        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dự án này"));

        boolean isMember =this.projectMemberRepository.existsByProjectIdAndUserId( id, currentUser.getId() );

        if (!isMember) {
            throw new RuntimeException("Quyền truy cập bị từ chối");
        }

        return toResponse(project);
    }
}
