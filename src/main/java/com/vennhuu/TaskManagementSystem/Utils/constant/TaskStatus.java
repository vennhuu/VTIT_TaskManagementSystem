package com.vennhuu.TaskManagementSystem.Utils.constant;

import lombok.Getter;

@Getter
public enum TaskStatus {
    TODO("Chờ xử lí"),
    IN_PROGRESS("Đang thực hiện"),
    DONE("Hoàn thành");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }
}