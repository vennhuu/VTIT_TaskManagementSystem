package com.vennhuu.TaskManagementSystem.Entity.res.activityLog;

import java.time.Instant;

import com.vennhuu.TaskManagementSystem.Utils.constant.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityLogResponse {

    private Long id;

    private Long taskId;

    private Long userId;

    private String fullName;

    private TaskStatus fromStatus;

    private TaskStatus toStatus;

    private Instant updatedAt;
}