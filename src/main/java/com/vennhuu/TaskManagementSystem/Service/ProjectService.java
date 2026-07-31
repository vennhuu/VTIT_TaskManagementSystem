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
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Service.cache.ProjectCacheService;
import com.vennhuu.TaskManagementSystem.Entity.req.project.ProjectReq;
import com.vennhuu.TaskManagementSystem.Service.mapper.ProjectMapper;
import com.vennhuu.TaskManagementSystem.Utils.AuthContextHolder;
import com.vennhuu.TaskManagementSystem.Utils.constant.ProjectRole;
import com.vennhuu.TaskManagementSystem.Utils.errors.ForbiddenException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectCacheService projectCacheService;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectCacheService projectCacheService
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectCacheService = projectCacheService;
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO getMyProjects(Specification<Project> spec, Pageable pageable) {
        Page<Project> page = projectRepository.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setCurrentPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotalPages(page.getTotalPages());
        meta.setTotalElements(page.getTotalElements());

        List<ProjectResponse> projects = page.getContent()
                .stream()
                .map(ProjectMapper::toResponse)
                .toList();

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(projects);
        return result;
    }

    public ProjectResponse createProject(ProjectReq request) {
        User currentUser = AuthContextHolder.getCurrentUser(userRepository);

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedBy(currentUser);
        projectRepository.save(project);

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(currentUser);
        member.setRole(ProjectRole.OWNER);
        projectMemberRepository.save(member);

        return ProjectMapper.toResponse(project);
    }

    public ProjectResponse updateProject(Long id, ProjectReq request) {
        User currentUser = AuthContextHolder.getCurrentUser(userRepository);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy project"));

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(id, currentUser.getId());

        if (member == null || member.getRole() != ProjectRole.OWNER) {
            throw new ForbiddenException("Chỉ có chủ sở hữu mới được phép cập nhật");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectRepository.save(project);

        projectCacheService.evict(id);

        return ProjectMapper.toResponse(project);
    }

    public void deleteProject(Long id) {
        User currentUser = AuthContextHolder.getCurrentUser(userRepository);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy project"));

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(id, currentUser.getId());

        if (member == null || member.getRole() != ProjectRole.OWNER) {
            throw new ForbiddenException("Chỉ có chủ sở hữu mới được phép xóa");
        }

        projectCacheService.evict(id);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        User currentUser = AuthContextHolder.getCurrentUser(userRepository);

        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(id, currentUser.getId());
        if (!isMember) {
            throw new ForbiddenException("Quyền truy cập bị từ chối");
        }

        return projectCacheService.getProjectCached(id);
    }
}
