package com.vennhuu.TaskManagementSystem.Entity.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignTaskEmailMessage {

    private String email;

    private String fullName;

    private String taskTitle;

    private String projectName;

    private String dueDate;
}