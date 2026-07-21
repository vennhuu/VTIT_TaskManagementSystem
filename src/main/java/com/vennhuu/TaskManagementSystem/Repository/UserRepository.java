package com.vennhuu.TaskManagementSystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vennhuu.TaskManagementSystem.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public User findByEmail( String email ) ;

    boolean existsByEmail( String email ) ;
    
}
