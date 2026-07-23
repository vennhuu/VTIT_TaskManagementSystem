package com.vennhuu.TaskManagementSystem.Entity.req.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectReq {

    @NotBlank(message="Không được để trống")
    private String name;

    private String description;
}
