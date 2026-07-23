package com.vennhuu.TaskManagementSystem.Entity.req.member;

import com.vennhuu.TaskManagementSystem.Utils.constant.ProjectRole;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberReq {
    
    @NotBlank(message="Không được để trống email")
    private String email ;
    private ProjectRole role ;
}
