package com.vennhuu.TaskManagementSystem.Entity.res.member;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.vennhuu.TaskManagementSystem.Utils.constant.ProjectRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponse {

    private Long id;

    private Long projectId;

    private Long userId;

    private String fullName;

    private String email;

    private ProjectRole role;

    private Instant joinedAt;
}