package com.vennhuu.TaskManagementSystem.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vennhuu.TaskManagementSystem.Entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    
    Optional<RefreshToken> findByToken( String name ) ;

    void deleteByToken(String token);
}
