package com.vennhuu.TaskManagementSystem.Entity.req.task;

import java.time.LocalDate;

import com.vennhuu.TaskManagementSystem.Utils.constant.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskReq {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    private TaskStatus status;

    private LocalDate dueDate;

    @NotNull(message = "Thiếu assignee")
    private Long assigneeId;
}
