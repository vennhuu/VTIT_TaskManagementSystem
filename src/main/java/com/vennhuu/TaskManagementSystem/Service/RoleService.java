package com.vennhuu.TaskManagementSystem.Service;

import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Role;
import com.vennhuu.TaskManagementSystem.Repository.RoleRepository;

@Service
public class RoleService {
    
    private final RoleRepository roleRepository ;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role findByName ( String name ) {
        return this.roleRepository.findByName(name) ;
    }
}
