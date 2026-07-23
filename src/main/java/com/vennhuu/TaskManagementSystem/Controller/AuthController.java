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
import com.vennhuu.TaskManagementSystem.Entity.res.auth.ResLoginDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.auth.UserResponse;
import com.vennhuu.TaskManagementSystem.Service.RefreshTokenService;
import com.vennhuu.TaskManagementSystem.Service.RoleService;
import com.vennhuu.TaskManagementSystem.Service.UserService;
import com.vennhuu.TaskManagementSystem.Utils.SecurityUtil;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final PasswordEncoder passwordEncoder ;
    private final UserService userService ;
    private final AuthenticationManager authenticationManager ;
    private final SecurityUtil securityUtil ;
    private final RoleService roleService ;
    private final RefreshTokenService refreshTokenService ;

    @Value("${venn.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration ;

    public AuthController(
            PasswordEncoder passwordEncoder, 
            UserService userService, 
            AuthenticationManager authenticationManager, 
            SecurityUtil securityUtil, 
            RoleService roleService,
            RefreshTokenService refreshTokenService
        ) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService ;
        this.authenticationManager = authenticationManager ;
        this.securityUtil = securityUtil ;
        this.roleService = roleService ;
        this.refreshTokenService = refreshTokenService ;
    }

    @PostMapping("/register")
    @APIMessage("Register a new user")
    public ResponseEntity<UserResponse> register(@RequestBody User newUser) throws Exception {

        if ( this.userService.existsByEmail(newUser.getEmail()) ) {
            throw new Exception("Email này đã tồn tại") ;
        }
        String hashPassword = this.passwordEncoder.encode(newUser.getPassword()) ;
        newUser.setPassword(hashPassword);

        Role r = this.roleService.findByName("ROLE_USER") ;
        newUser.setRole(r);
        this.userService.saveUser(newUser) ;
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createNewUser(newUser));
    }

    @PostMapping("/login")
    @APIMessage("Login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginReq user, HttpServletRequest request) {

        // nạp input gồm email và password vào security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()) ;

        // xác thực người dùng 
        Authentication authentication = this.authenticationManager.authenticate(authenticationToken) ;

        // set thông tin người dùng đăng nhập vào context (có thể sử dụng sau này)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO() ;
        User currentUserDB = this.userService.findByEmail(user.getEmail()) ;

        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getFullName());
            res.setUser(userLogin);
        }

        // create access token
        String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);
        res.setAccessToken(access_token);

        // create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(user.getEmail(), res);

        // update user
        String device = request.getHeader("User-Agent");

        this.refreshTokenService.updateUserToken(refresh_token, user.getEmail(), device);

        // set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }
    
    @GetMapping("/account")
    @APIMessage("Fetch account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
 
        User currentUserDB = this.userService.findByEmail(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getFullName());

            userGetAccount.setUser(userLogin);
        }

        return ResponseEntity.ok().body(userGetAccount);
    }
    
    @PostMapping("/logout")
    @APIMessage("Logout account")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {

        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    @APIMessage("Refresh access token")
    public ResponseEntity<ResLoginDTO> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken) throws Exception {

        // 1. Kiểm tra cookie có tồn tại không
        if (refreshToken == null) {
            throw new Exception("Không tìm thấy Refresh Token.");
        }

        // 2. Kiểm tra JWT Refresh Token có hợp lệ không
        Jwt decodedJwt = this.securityUtil.checkValidRefreshToken(refreshToken);

        String email = decodedJwt.getSubject();

        // 3. Kiểm tra Refresh Token có trong database không
        RefreshToken currentRefreshToken =
                this.refreshTokenService.findByToken(refreshToken);

        if (currentRefreshToken == null) {
            throw new Exception("Refresh Token không hợp lệ.");
        }

        // 4. Lấy User
        User currentUser = currentRefreshToken.getUser();

        // 5. Tạo dữ liệu trả về
        ResLoginDTO res = new ResLoginDTO();

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getFullName()
        );

        res.setUser(userLogin);

        // 6. Sinh Access Token mới
        String accessToken = this.securityUtil.createAccessToken(email, res);

        res.setAccessToken(accessToken);

        // 7. Sinh Refresh Token mới
        String newRefreshToken = this.securityUtil.createRefreshToken(email, res);

        // 8. Xóa Refresh Token cũ
        this.refreshTokenService.deleteByToken(refreshToken);

        // 9. Lưu Refresh Token mới
        this.refreshTokenService.saveRefreshToken( newRefreshToken, currentUser );

        // 10. Gửi Cookie mới
        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(res);
    }
}
