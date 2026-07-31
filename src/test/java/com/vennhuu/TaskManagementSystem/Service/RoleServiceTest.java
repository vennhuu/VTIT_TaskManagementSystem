package com.vennhuu.TaskManagementSystem.Service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vennhuu.TaskManagementSystem.Entity.Role;
import com.vennhuu.TaskManagementSystem.Repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock 
    private RoleRepository roleRepository;

    @InjectMocks 
    private RoleService roleService;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");
    }

    @Test
    @DisplayName("Should return role when found by name")
    void shouldReturnRoleWhenFoundByName() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);

        Role result = roleService.findByName("ROLE_USER");

        assertThat(result).isEqualTo(role);
        verify(roleRepository).findByName("ROLE_USER");
    }

    @Test
    @DisplayName("Should return null when role not found by name")
    void shouldReturnNullWhenNotFound() {
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(null);

        Role result = roleService.findByName("ROLE_UNKNOWN");

        assertThat(result).isNull();
    }
}
