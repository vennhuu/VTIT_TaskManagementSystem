package com.vennhuu.TaskManagementSystem.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vennhuu.TaskManagementSystem.Entity.Role;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.res.auth.UserResponse;
import com.vennhuu.TaskManagementSystem.Repository.RoleRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getFullName());
        response.setCreatedAt(user.getCreatedAt());

        Role role = roleRepository.findByName("ROLE_USER");
        if (role != null) {
            UserResponse.RoleUser roleUser = new UserResponse.RoleUser();
            roleUser.setId(role.getId());
            roleUser.setName(role.getName());
            response.setRole(roleUser);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
