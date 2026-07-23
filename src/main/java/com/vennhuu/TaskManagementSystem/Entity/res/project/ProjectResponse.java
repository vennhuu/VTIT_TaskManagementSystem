package com.vennhuu.TaskManagementSystem.Entity.res.project;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProjectResponse {
    
    private Long id;

    private String name;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;

    private Owner owner;

    @Getter
    @Setter
    public static class Owner {

        private Long id;

        private String fullName;

        private String email;

    }
}
