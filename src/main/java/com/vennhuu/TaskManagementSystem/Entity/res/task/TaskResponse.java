package com.vennhuu.TaskManagementSystem.Entity.res.task;

import java.time.Instant;

import com.vennhuu.TaskManagementSystem.Utils.constant.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private Instant dueDate;

    private Instant createdAt;

    private Long projectId;

    private Long createdById;

    private String createdByName;

    private Long assigneeId;

    private String assigneeName;
}
