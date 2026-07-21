package com.vennhuu.TaskManagementSystem.Utils.constant;

import lombok.Getter;

@Getter
public enum ProjectRole {
    OWNER("Người tạo"),

    MEMBER("Thành viên") ;

    private final String description ;

    private ProjectRole(String description) {
        this.description = description;
    }
    
}
