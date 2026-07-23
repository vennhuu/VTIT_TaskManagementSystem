package com.vennhuu.TaskManagementSystem.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.member.MemberReq;
import com.vennhuu.TaskManagementSystem.Entity.res.member.MemberResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Utils.SecurityUtil;
import com.vennhuu.TaskManagementSystem.Utils.constant.ProjectRole;

import jakarta.transaction.Transactional;

@Service
public class ProjectMemberService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository
        ) {

        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    private User getCurrentUser() {

        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        return userRepository.findByEmail(email);
    }

    public ProjectMember save ( ProjectMember pro) {
        return this.projectMemberRepository.save(pro) ;
    }

    private void checkOwner(Long projectId) {

        User currentUser = getCurrentUser();

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUser.getId());

        if (member == null) {
            throw new RuntimeException("Bạn không thuộc project này");
        }

        if (member.getRole() != ProjectRole.OWNER) {
            throw new RuntimeException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    @Transactional
    public MemberResponse addMember(Long projectId, MemberReq req) {

        // Kiểm tra project
        Project project = this.projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project không tồn tại"));

        // Kiểm tra user
        User user = this.userRepository.findByEmail(req.getEmail());

        if (user == null) {
            throw new RuntimeException("Email không tồn tại");
        }

        // Kiểm tra đã là member chưa
        boolean exists = this.projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());

        if (exists) {
            throw new RuntimeException("Người dùng này đã là thành viên của project");
        }

        // Tạo member
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(req.getRole());

        ProjectMember saved = projectMemberRepository.save(member);

        return convertToMemberResponse(saved);
    }

    private MemberResponse convertToMemberResponse(ProjectMember member) {

        MemberResponse response = new MemberResponse();

        response.setId(member.getId());
        response.setProjectId(member.getProject().getId());
        response.setUserId(member.getUser().getId());
        response.setFullName(member.getUser().getFullName());
        response.setEmail(member.getUser().getEmail());
        response.setRole(member.getRole());
        response.setJoinedAt(member.getJoinedAt());

        return response;
    }

    public void deleteMember(Long projectId, Long memberId) {

        checkOwner(projectId);

        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy member"));

        if (!member.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Member không thuộc project này");
        }

        if (member.getRole() == ProjectRole.OWNER) {
            throw new RuntimeException("Không thể xóa OWNER");
        }

        projectMemberRepository.delete(member);
    }

    public MemberResponse updateRoleMember(
            Long projectId,
            Long memberId,
            MemberReq req) {

        checkOwner(projectId);

        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy member"));

        if (!member.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Member không thuộc project này");
        }

        member.setRole(req.getRole());

        ProjectMember saved = projectMemberRepository.save(member);

        return convertToMemberResponse(saved);
    }
    
     public List<MemberResponse> getAllMembers(Long projectId) {

        List<ProjectMember> members = this.projectMemberRepository.findByProjectId(projectId);

        return members.stream()
                .map(this::convertToMemberResponse)
                .toList();
    }
}
