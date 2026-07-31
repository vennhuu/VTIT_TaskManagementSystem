package com.vennhuu.TaskManagementSystem.Service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.vennhuu.TaskManagementSystem.Entity.RefreshToken;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Repository.RefreshTokenRepository;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock 
    private UserRepository userRepository;

    @Mock 
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks 
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        // Inject @Value field manually
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", 604800L);

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("sample-token");
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
    }

    @Nested
    @DisplayName("createToken(token, email, device)")
    class CreateTokenByEmail {

        @Test
        @DisplayName("Should create and save token when user exists")
        void shouldCreateTokenWhenUserExists() {
            when(userRepository.findByEmail("test@test.com")).thenReturn(user);
            when(refreshTokenRepository.save(any())).thenReturn(refreshToken);

            refreshTokenService.createToken("sample-token", "test@test.com", "Chrome/Windows");

            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should do nothing when user not found by email")
        void shouldDoNothingWhenUserNotFound() {
            when(userRepository.findByEmail("notfound@test.com")).thenReturn(null);

            refreshTokenService.createToken("sample-token", "notfound@test.com", "Chrome");

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("createToken(token, user)")
    class CreateTokenByUser {

        @Test
        @DisplayName("Should create token without device")
        void shouldCreateTokenWithoutDevice() {
            when(refreshTokenRepository.save(any())).thenReturn(refreshToken);

            refreshTokenService.createToken("sample-token", user);

            verify(refreshTokenRepository).save(argThat(rt ->
                    rt.getToken().equals("sample-token")
                    && rt.getUser().equals(user)
                    && rt.getDevice() == null
            ));
        }
    }

    @Nested
    @DisplayName("revokeToken()")
    class RevokeToken {

        @Test
        @DisplayName("Should mark token as revoked")
        void shouldRevokeToken() {
            when(refreshTokenRepository.findByToken("sample-token")).thenReturn(Optional.of(refreshToken));
            when(refreshTokenRepository.save(any())).thenReturn(refreshToken);

            refreshTokenService.revokeToken("sample-token");

            assertThat(refreshToken.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(refreshToken);
        }

        @Test
        @DisplayName("Should do nothing when token not found")
        void shouldDoNothingWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken("ghost-token")).thenReturn(Optional.empty());

            refreshTokenService.revokeToken("ghost-token");

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findByToken()")
    class FindByToken {

        @Test
        @DisplayName("Should return token entity when found")
        void shouldReturnToken() {
            when(refreshTokenRepository.findByToken("sample-token")).thenReturn(Optional.of(refreshToken));

            RefreshToken result = refreshTokenService.findByToken("sample-token");

            assertThat(result).isEqualTo(refreshToken);
        }

        @Test
        @DisplayName("Should return null when token not found")
        void shouldReturnNullWhenNotFound() {
            when(refreshTokenRepository.findByToken("ghost-token")).thenReturn(Optional.empty());

            RefreshToken result = refreshTokenService.findByToken("ghost-token");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("deleteByToken()")
    class DeleteByToken {

        @Test
        @DisplayName("Should call repository deleteByToken")
        void shouldDeleteToken() {
            refreshTokenService.deleteByToken("sample-token");

            verify(refreshTokenRepository).deleteByToken("sample-token");
        }
    }
}
