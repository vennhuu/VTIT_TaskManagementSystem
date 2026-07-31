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

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectMemberService Unit Tests")
class ProjectMemberServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private User ownerUser;
    private User memberUser;
    private Project project;
    private ProjectMember ownerMembership;
    private ProjectMember memberMembership;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setFullName("Owner");
        ownerUser.setEmail("owner@test.com");

        memberUser = new User();
        memberUser.setId(2L);
        memberUser.setFullName("Member");
        memberUser.setEmail("member@test.com");

        project = new Project();
        project.setId(10L);
        project.setName("Test Project");

        ownerMembership = new ProjectMember();
        ownerMembership.setId(1L);
        ownerMembership.setProject(project);
        ownerMembership.setUser(ownerUser);
        ownerMembership.setRole(ProjectRole.OWNER);

        memberMembership = new ProjectMember();
        memberMembership.setId(2L);
        memberMembership.setProject(project);
        memberMembership.setUser(memberUser);
        memberMembership.setRole(ProjectRole.MEMBER);
    }

    @Nested
    @DisplayName("addMember()")
    class AddMember {

        @Test
        @DisplayName("Should add member successfully when OWNER requests")
        void shouldAddMemberSuccessfully() {
            MemberReq req = new MemberReq();
            req.setEmail("member@test.com");
            req.setRole(ProjectRole.MEMBER);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(userRepository.findByEmail("member@test.com")).thenReturn(memberUser);
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(false);
            when(projectMemberRepository.save(any())).thenReturn(memberMembership);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                MemberResponse response = projectMemberService.addMember(10L, req);

                assertThat(response).isNotNull();
                assertThat(response.getRole()).isEqualTo(ProjectRole.MEMBER);
                verify(projectMemberRepository).save(any(ProjectMember.class));
            }
        }

        @Test
        @DisplayName("Should default role to MEMBER when req.getRole() is null")
        void shouldDefaultRoleToMember() {
            MemberReq req = new MemberReq();
            req.setEmail("member@test.com");
            req.setRole(null); // null → default MEMBER

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(userRepository.findByEmail("member@test.com")).thenReturn(memberUser);
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(false);
            when(projectMemberRepository.save(any())).thenReturn(memberMembership);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                MemberResponse response = projectMemberService.addMember(10L, req);

                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when project not found")
        void shouldThrowWhenProjectNotFound() {
            MemberReq req = new MemberReq();
            req.setEmail("member@test.com");

            when(projectRepository.findById(99L)).thenReturn(Optional.empty());

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                assertThatThrownBy(() -> projectMemberService.addMember(99L, req))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Project không tồn tại");
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when requester has no membership")
        void shouldThrowWhenNoMembership() {
            MemberReq req = new MemberReq();
            req.setEmail("member@test.com");

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(null);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                assertThatThrownBy(() -> projectMemberService.addMember(10L, req))
                        .isInstanceOf(ForbiddenException.class)
                        .hasMessageContaining("không thuộc project");
            }
        }

        @Test
        @DisplayName("Should throw ForbiddenException when requester is not OWNER")
        void shouldThrowWhenNotOwner() {
            MemberReq req = new MemberReq();
            req.setEmail("member@test.com");

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(memberMembership);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                assertThatThrownBy(() -> projectMemberService.addMember(10L, req))
                        .isInstanceOf(ForbiddenException.class)
                        .hasMessageContaining("OWNER mới được thêm thành viên");
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when email not found")
        void shouldThrowWhenEmailNotFound() {
            MemberReq req = new MemberReq();
            req.setEmail("notfound@test.com");

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(userRepository.findByEmail("notfound@test.com")).thenReturn(null);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                assertThatThrownBy(() -> projectMemberService.addMember(10L, req))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Email không tồn tại");
            }
        }

        @Test
        @DisplayName("Should throw ConflictException when user already a member")
        void shouldThrowWhenAlreadyMember() {
            MemberReq req = new MemberReq();
            req.setEmail("member@test.com");

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(userRepository.findByEmail("member@test.com")).thenReturn(memberUser);
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                assertThatThrownBy(() -> projectMemberService.addMember(10L, req))
                        .isInstanceOf(ConflictException.class)
                        .hasMessageContaining("đã là thành viên");
            }
        }
    }

    @Nested
    @DisplayName("deleteMember()")
    class DeleteMember {

        @Test
        @DisplayName("Should delete member successfully")
        void shouldDeleteMemberSuccessfully() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L)).thenReturn(memberMembership);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                projectMemberService.deleteMember(10L, 2L);

                verify(projectMemberRepository).delete(memberMembership);
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when target member not found")
        void shouldThrowWhenMemberNotFound() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 99L)).thenReturn(null);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                assertThatThrownBy(() -> projectMemberService.deleteMember(10L, 99L))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("User không thuộc project này");
            }
        }

        @Test
        @DisplayName("Should throw BadRequestException when trying to delete OWNER")
        void shouldThrowWhenDeletingOwner() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            // Target is also an OWNER
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 3L)).thenReturn(ownerMembership);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                assertThatThrownBy(() -> projectMemberService.deleteMember(10L, 3L))
                        .isInstanceOf(BadRequestException.class)
                        .hasMessageContaining("Không thể xóa OWNER");
            }
        }
    }

    @Nested
    @DisplayName("updateRoleMember()")
    class UpdateRoleMember {

        @Test
        @DisplayName("Should update member role successfully")
        void shouldUpdateRoleSuccessfully() {
            MemberReq req = new MemberReq();
            req.setRole(ProjectRole.OWNER);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L)).thenReturn(memberMembership);
            when(projectMemberRepository.save(any())).thenReturn(memberMembership);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                MemberResponse response = projectMemberService.updateRoleMember(10L, 2L, req);

                assertThat(response).isNotNull();
                verify(projectMemberRepository).save(memberMembership);
            }
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when target user not found")
        void shouldThrowWhenTargetNotFound() {
            MemberReq req = new MemberReq();
            req.setRole(ProjectRole.OWNER);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L)).thenReturn(ownerMembership);
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 99L)).thenReturn(null);

            try (MockedStatic<AuthContextHolder> mocked = mockStatic(AuthContextHolder.class)) {
                mocked.when(() -> AuthContextHolder.getCurrentUser(userRepository)).thenReturn(ownerUser);

                assertThatThrownBy(() -> projectMemberService.updateRoleMember(10L, 99L, req))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("User không thuộc project này");
            }
        }
    }

    @Nested
    @DisplayName("getAllMembers()")
    class GetAllMembers {

        @Test
        @DisplayName("Should return paginated list of members")
        void shouldReturnPaginatedMembers() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ProjectMember> page = new PageImpl<>(List.of(ownerMembership, memberMembership), pageable, 2);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findAllByProjectId(eq(10L), any(Specification.class), eq(pageable)))
                    .thenReturn(page);

            ResultPaginationDTO result = projectMemberService.getAllMembers(10L, mock(Specification.class), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getMeta().getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when project not found")
        void shouldThrowWhenProjectNotFound() {
            when(projectRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    projectMemberService.getAllMembers(99L, mock(Specification.class), PageRequest.of(0, 10)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Project không tồn tại");
        }
    }
}
