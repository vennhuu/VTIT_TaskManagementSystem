package com.vennhuu.TaskManagementSystem.Service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vennhuu.TaskManagementSystem.Entity.Role;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.res.auth.UserResponse;
import com.vennhuu.TaskManagementSystem.Repository.RoleRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setFullName("Test User");
        user.setRole(role);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should save and return user")
        void shouldSaveUser() {
            when(userRepository.save(user)).thenReturn(user);

            User result = userService.save(user);

            assertThat(result).isEqualTo(user);
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("toUserResponse")
    class ToUserResponse {

        @Test
        @DisplayName("Should map user to UserResponse with role")
        void shouldMapUserWithRole() {
            when(roleRepository.findByName("ROLE_USER")).thenReturn(role);

            UserResponse response = userService.toUserResponse(user);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("test@test.com");
            assertThat(response.getName()).isEqualTo("Test User");
            assertThat(response.getRole()).isNotNull();
            assertThat(response.getRole().getName()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("Should map user to UserResponse without role when role not found")
        void shouldMapUserWithoutRole() {
            when(roleRepository.findByName("ROLE_USER")).thenReturn(null);

            UserResponse response = userService.toUserResponse(user);

            assertThat(response).isNotNull();
            assertThat(response.getRole()).isNull();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("Should return user when email exists")
        void shouldReturnUserWhenEmailExists() {
            when(userRepository.findByEmail("test@test.com")).thenReturn(user);

            User result = userService.findByEmail("test@test.com");

            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("Should return null when email not found")
        void shouldReturnNullWhenNotFound() {
            when(userRepository.findByEmail("notfound@test.com")).thenReturn(null);

            User result = userService.findByEmail("notfound@test.com");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

            boolean result = userService.existsByEmail("test@test.com");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when email not found")
        void shouldReturnFalseWhenNotFound() {
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

            boolean result = userService.existsByEmail("new@test.com");

            assertThat(result).isFalse();
        }
    }
}
