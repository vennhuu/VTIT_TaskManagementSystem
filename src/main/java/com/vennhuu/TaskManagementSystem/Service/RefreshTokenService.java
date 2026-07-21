package com.vennhuu.TaskManagementSystem.Service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.RefreshToken;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Repository.RefreshTokenRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    @Value("${venn.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository ;
    private final RefreshTokenRepository refreshTokenRepository ;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    public void updateUserToken( String token, String email, String device ) {

        User currentUser = this.userRepository.findByEmail(email);

        if (currentUser == null) {
            return;
        }

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(token);
        refreshToken.setUser(currentUser);

        refreshToken.setDevice(device);

        refreshToken.setCreatedAt(Instant.now());

        refreshToken.setExpiredAt(
                Instant.now()
                        .plusSeconds(refreshTokenExpiration)
        );

        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
    }

    public void revokeToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElse(null);

        if (refreshToken != null) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
    }

    public RefreshToken findByToken( String token ) {
        return this.refreshTokenRepository.findByToken(token).orElse(null) ;
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public void saveRefreshToken(String token, User user) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiredAt(
                Instant.now().plusSeconds(refreshTokenExpiration)
        );

        refreshTokenRepository.save(refreshToken);
    }
}
