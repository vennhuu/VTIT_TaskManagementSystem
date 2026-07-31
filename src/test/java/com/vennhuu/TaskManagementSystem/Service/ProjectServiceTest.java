package com.vennhuu.TaskManagementSystem.Service;

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
import com.vennhuu.TaskManagementSystem.Entity.ProjectMember;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.project.ProjectReq;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.project.ProjectResponse;
import com.vennhuu.TaskManagementSystem.Repository.ProjectMemberRepository;
import com.vennhuu.TaskManagementSystem.Repository.ProjectRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Service.cache.ProjectCacheService;
import com.vennhuu.TaskManagementSystem.Utils.AuthContextHolder;
import com.vennhuu.TaskManagementSystem.Utils.constant.ProjectRole;
import com.vennhuu.TaskManagementSystem.Utils.errors.ForbiddenException;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock 
    private ProjectRepository projectRepository;

    @Mock 
    private UserRepository userRepository;

    @Mock 
    private ProjectMemberRepository projectMemberRepository;

    @Mock 
    private ProjectCacheService projectCacheService;


    @InjectMocks 
    private ProjectService projectService;


    private User owner;
    private Project project;
    private ProjectMember ownerMembership;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setFullName("Owner User");
        owner.setEmail("owner@test.com");

        project = new Project();
        project.setId(10L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setCreatedBy(owner);

        ownerMembership = new ProjectMember();
        ownerMembership.setProject(project);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(ProjectRole.OWNER);
    }

    @Nested
    @DisplayName("getMyProjects()")
    class GetMyProjects {

        @Test
        @DisplayName("Should return paginated list of projects")
        void shouldReturnPaginatedProjects() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Project> page = new PageImpl<>(List.of(project), pageable, 1);

            when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            ResultPaginationDTO result = projectService.getMyProjects(mock(Specification.class), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getMeta().getTotalElements()).isEqualTo(1);
            assertThat(result.getMeta().getCurrentPage()).isEqualTo(1);
            assertThat(result.getMeta().getPageSize()).isEqualTo(10);
        }
    }


    @Nested
    @DisplayName("createProject()")
    class CreateProject {

        @Test
        @DisplayName("Should create project and add current user as OWNER")
        void shouldCreateProjectSuccessfully() {
            ProjectReq req = new ProjectReq();
            req.setName("New Project");
            req.setDescription("Description");

            when(projectRepository.save(any())).thenReturn(project);
            when(projectMemberRepository.save(any())).thenReturn(ownerMembership);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                ProjectResponse response = projectService.createProject(req);

                assertThat(response).isNotNull();
                verify(projectRepository).save(any(Project.class));
                verify(projectMemberRepository).save(any(ProjectMember.class));
            }
        }
    }


    @Nested
    @DisplayName("updateProject()")
    class UpdateProject {

        @Test
        @DisplayName("Should update project when current user is OWNER")
        void shouldUpdateProjectSuccessfully() {
            ProjectReq req = new ProjectReq();
            req.setName("Updated Name");
            req.setDescription("Updated Desc");

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(projectRepository.save(any())).thenReturn(project);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                ProjectResponse response = projectService.updateProject(10L, req);

                assertThat(response).isNotNull();
                verify(projectRepository).save(project);
                verify(projectCacheService).evict(10L);
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when project not found")
        void shouldThrowWhenProjectNotFound() {
            ProjectReq req = new ProjectReq();
            req.setName("X");

            when(projectRepository.findById(99L)).thenReturn(Optional.empty());

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                assertThatThrownBy(() -> projectService.updateProject(99L, req))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Không tìm thấy project");
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user is not OWNER")
        void shouldThrowWhenNotOwner() {
            ProjectReq req = new ProjectReq();
            req.setName("X");

            ProjectMember memberRole = new ProjectMember();
            memberRole.setRole(ProjectRole.MEMBER);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(memberRole);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                assertThatThrownBy(() -> projectService.updateProject(10L, req))
                        .isInstanceOf(ForbiddenException.class)
                        .hasMessageContaining("chủ sở hữu");
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user has no membership")
        void shouldThrowWhenNoMembership() {
            ProjectReq req = new ProjectReq();
            req.setName("X");

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(null);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                assertThatThrownBy(() -> projectService.updateProject(10L, req))
                        .isInstanceOf(ForbiddenException.class);
            }
        }
    }

    @Nested
    @DisplayName("deleteProject()")
    class DeleteProject {

        @Test
        @DisplayName("Should delete project when OWNER")
        void shouldDeleteSuccessfully() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                projectService.deleteProject(10L);

                verify(projectRepository).delete(project);
                verify(projectCacheService).evict(10L);
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when project not found")
        void shouldThrowWhenProjectNotFound() {
            when(projectRepository.findById(99L)).thenReturn(Optional.empty());

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                assertThatThrownBy(() -> projectService.deleteProject(99L))
                        .isInstanceOf(ResourceNotFoundException.class);
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user is not OWNER")
        void shouldThrowWhenNotOwner() {
            ProjectMember memberRole = new ProjectMember();
            memberRole.setRole(ProjectRole.MEMBER);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(memberRole);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                assertThatThrownBy(() -> projectService.deleteProject(10L))
                        .isInstanceOf(ForbiddenException.class)
                        .hasMessageContaining("chủ sở hữu");
            }
        }
    }

    @Nested
    @DisplayName("getProject()")
    class GetProject {

        @Test
        @DisplayName("Should return cached project when user is member")
        void shouldReturnProjectWhenMember() {
            ProjectResponse cached = new ProjectResponse();
            cached.setId(10L);

            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(true);
            when(projectCacheService.getProjectCached(10L)).thenReturn(cached);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                ProjectResponse result = projectService.getProject(10L);

                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(10L);
                verify(projectCacheService).getProjectCached(10L);
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user is not member")
        void shouldThrowWhenNotMember() {
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 1L)).thenReturn(false);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(owner);

                assertThatThrownBy(() -> projectService.getProject(10L))
                        .isInstanceOf(ForbiddenException.class)
                        .hasMessageContaining("Quyền truy cập bị từ chối");
            }
        }
    }
}
