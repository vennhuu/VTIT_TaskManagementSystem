package com.vennhuu.TaskManagementSystem.Service;

import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Role;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.res.auth.UserResponse;
import com.vennhuu.TaskManagementSystem.Repository.RoleRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository ;
    private final RoleRepository roleRepository ;

    public UserService(UserRepository userRepository, RoleRepository roleRepository ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository ;
    }

    public User saveUser(User user){
        return this.userRepository.save(user) ;
    }

    public UserResponse createNewUser( User user ) {

        UserResponse newUser = new UserResponse() ;
        newUser.setId(user.getId());
        newUser.setEmail(user.getEmail());
        newUser.setName(user.getFullName());
        newUser.setCreatedAt(user.getCreatedAt());

        Role r = this.roleRepository.findByName("ROLE_USER") ;

        UserResponse.RoleUser role = new UserResponse.RoleUser() ;
        role.setId(r.getId());
        role.setName(r.getName());
        newUser.setRole(role);

        return newUser ;
    }

    public User findByEmail ( String email ) {
        return this.userRepository.findByEmail(email) ;
    }

    public boolean existsByEmail ( String email ) {
        return this.userRepository.existsByEmail(email) ;
    }


}
