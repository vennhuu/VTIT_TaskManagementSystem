package com.vennhuu.TaskManagementSystem.Entity.req.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq {

    @NotBlank(message="Không được để trống email")
    private String email ;

    @NotBlank(message="Không được để trống mật khẩu")
    private String password ;
}
