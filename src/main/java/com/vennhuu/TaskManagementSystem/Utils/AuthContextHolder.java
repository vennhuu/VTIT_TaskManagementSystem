package com.vennhuu.TaskManagementSystem.Utils;

import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Utils.errors.ResourceNotFoundException;
import com.vennhuu.TaskManagementSystem.Utils.errors.UnauthorizedException;

public final class AuthContextHolder {

    private AuthContextHolder() {}

    public static User getCurrentUser(UserRepository userRepository) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new UnauthorizedException("Bạn chưa đăng nhập"));

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng");
        }
        return user;
    }
}
