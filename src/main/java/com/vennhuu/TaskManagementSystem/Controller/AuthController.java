package com.vennhuu.TaskManagementSystem.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vennhuu.TaskManagementSystem.Entity.RefreshToken;
import com.vennhuu.TaskManagementSystem.Entity.Role;
import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.req.auth.LoginReq;
import com.vennhuu.TaskManagementSystem.Entity.req.auth.RegisterReq;
import com.vennhuu.TaskManagementSystem.Entity.res.auth.ResLoginDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.auth.UserResponse;
import com.vennhuu.TaskManagementSystem.Service.RefreshTokenService;
import com.vennhuu.TaskManagementSystem.Service.RoleService;
import com.vennhuu.TaskManagementSystem.Service.UserService;
import com.vennhuu.TaskManagementSystem.Utils.SecurityUtil;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;
import com.vennhuu.TaskManagementSystem.Utils.errors.ExistMailException;
import com.vennhuu.TaskManagementSystem.Utils.errors.UnauthorizedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Đăng ký, đăng nhập, refresh token và quản lý phiên đăng nhập")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final RoleService roleService;
    private final RefreshTokenService refreshTokenService;

    @Value("${venn.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(
            PasswordEncoder passwordEncoder,
            UserService userService,
            AuthenticationManager authenticationManager,
            SecurityUtil securityUtil,
            RoleService roleService,
            RefreshTokenService refreshTokenService
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
        this.roleService = roleService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    @APIMessage("Register a new user")
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới với email và mật khẩu. Email phải là duy nhất trong hệ thống.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (validation)"),
        @ApiResponse(responseCode = "409", description = "Email đã tồn tại trong hệ thống")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterReq req) {
        if (userService.existsByEmail(req.getEmail())) {
            throw new ExistMailException("Email này đã được sử dụng");
        }

        Role defaultRole = roleService.findByName("ROLE_USER");

        User newUser = new User();
        newUser.setFullName(req.getFullName());
        newUser.setEmail(req.getEmail());
        newUser.setPassword(passwordEncoder.encode(req.getPassword()));
        newUser.setRole(defaultRole);

        userService.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.toUserResponse(newUser));
    }

    @PostMapping("/login")
    @APIMessage("Login")
    @Operation(summary = "Đăng nhập", description = "Xác thực bằng email/password, trả về Access Token và set Refresh Token vào HttpOnly cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
        @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng")
    })
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginReq req, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUser = userService.findByEmail(req.getEmail());

        ResLoginDTO res = new ResLoginDTO();
        if (currentUser != null) {
            res.setUser(new ResLoginDTO.UserLogin(
                    currentUser.getId(),
                    currentUser.getEmail(),
                    currentUser.getFullName()
            ));
        }

        String accessToken = securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(accessToken);

        String refreshToken = securityUtil.createRefreshToken(req.getEmail(), res);
        refreshTokenService.createToken(refreshToken, req.getEmail(), request.getHeader("User-Agent"));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(refreshToken).toString())
                .body(res);
    }

    @GetMapping("/account")
    @APIMessage("Fetch account")
    @Operation(summary = "Lấy thông tin tài khoản hiện tại", description = "Trả về thông tin user đang đăng nhập dựa trên Access Token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");

        User currentUser = userService.findByEmail(email);
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUser != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
            userLogin.setId(currentUser.getId());
            userLogin.setEmail(currentUser.getEmail());
            userLogin.setName(currentUser.getFullName());
            userGetAccount.setUser(userLogin);
        }

        return ResponseEntity.ok(userGetAccount);
    }

    @PostMapping("/logout")
    @APIMessage("Logout account")
    @Operation(summary = "Đăng xuất", description = "Thu hồi Refresh Token và xóa cookie. Access Token sẽ hết hạn theo TTL của nó.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Đăng xuất thành công")
    })
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .build();
    }

    @PostMapping("/refresh")
    @APIMessage("Refresh access token")
    @Operation(summary = "Làm mới Access Token", description = "Dùng Refresh Token trong cookie để cấp Access Token mới. Refresh Token cũ sẽ bị xóa (rotation).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cấp Access Token mới thành công"),
        @ApiResponse(responseCode = "401", description = "Refresh Token không hợp lệ hoặc đã bị thu hồi")
    })
    public ResponseEntity<ResLoginDTO> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Không tìm thấy Refresh Token");
        }

        Jwt decodedJwt = securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedJwt.getSubject();

        RefreshToken storedToken = refreshTokenService.findByToken(refreshToken);
        if (storedToken == null) {
            throw new UnauthorizedException("Refresh Token không hợp lệ hoặc đã bị thu hồi");
        }

        User user = storedToken.getUser();
        ResLoginDTO res = new ResLoginDTO();
        res.setUser(new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getFullName()));

        String newAccessToken = securityUtil.createAccessToken(email, res);
        res.setAccessToken(newAccessToken);

        String newRefreshToken = securityUtil.createRefreshToken(email, res);

        refreshTokenService.deleteByToken(refreshToken);
        refreshTokenService.createToken(newRefreshToken, user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(newRefreshToken).toString())
                .body(res);
    }

    // Cookies
    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
    }
}
