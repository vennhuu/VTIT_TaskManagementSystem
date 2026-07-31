package com.vennhuu.TaskManagementSystem.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vennhuu.TaskManagementSystem.Entity.Project;
import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.member.MemberReq;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.member.MemberResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Utils.AuthContextHolder;
import com.vennhuu.TaskManagementSystem.Utils.constant.ProjectRole;
import com.vennhuu.TaskManagementSystem.Utils.errors.BadRequestException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ConflictException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ForbiddenException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@Service
@Transactional
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

    private void requireOwner(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project không tồn tại"));

        User currentUser = AuthContextHolder.getCurrentUser(userRepository);
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, currentUser.getId());

        if (member == null) {
            throw new ForbiddenException("Bạn không thuộc project này");
        }
        if (member.getRole() != ProjectRole.OWNER) {
            throw new ForbiddenException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    public MemberResponse addMember(Long projectId, MemberReq req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project không tồn tại"));

        User owner = AuthContextHolder.getCurrentUser(userRepository);
        ProjectMember ownerMembership = projectMemberRepository.findByProjectIdAndUserId(projectId, owner.getId());

        if (ownerMembership == null) {
            throw new ForbiddenException("Bạn không thuộc project này");
        }
        if (ownerMembership.getRole() != ProjectRole.OWNER) {
            throw new ForbiddenException("Chỉ OWNER mới được thêm thành viên");
        }

        User newMemberUser = userRepository.findByEmail(req.getEmail());
        if (newMemberUser == null) {
            throw new ResourceNotFoundException("Email không tồn tại trong hệ thống");
        }

        boolean alreadyMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, newMemberUser.getId());
        if (alreadyMember) {
            throw new ConflictException("Người dùng này đã là thành viên của project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(newMemberUser);
        member.setRole(req.getRole() != null ? req.getRole() : ProjectRole.MEMBER);

        return toMemberResponse(projectMemberRepository.save(member));
    }

    public void deleteMember(Long projectId, Long userId) {
        requireOwner(projectId);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            throw new ResourceNotFoundException("User không thuộc project này");
        }
        if (member.getRole() == ProjectRole.OWNER) {
            throw new BadRequestException("Không thể xóa OWNER khỏi project");
        }

        projectMemberRepository.delete(member);
    }

    public MemberResponse updateRoleMember(Long projectId, Long userId, MemberReq req) {
        requireOwner(projectId);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            throw new ResourceNotFoundException("User không thuộc project này");
        }

        member.setRole(req.getRole());
        return toMemberResponse(projectMemberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getAllMembers(Long projectId, Specification<ProjectMember> spec, Pageable pageable) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project không tồn tại"));

        Page<ProjectMember> page = projectMemberRepository.findAllByProjectId(projectId, spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setCurrentPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize());
        meta.setTotalElements(page.getTotalElements());
        meta.setTotalPages(page.getTotalPages());

        List<MemberResponse> members = page.getContent()
                .stream()
                .map(this::toMemberResponse)
                .toList();

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(members);
        return result;
    }

    private MemberResponse toMemberResponse(ProjectMember member) {
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
}
