package com.vennhuu.TaskManagementSystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vennhuu.TaskManagementSystem.Entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    public Role findByName( String name ) ;
}
