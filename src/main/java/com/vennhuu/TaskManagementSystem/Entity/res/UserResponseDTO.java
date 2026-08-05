package com.vennhuu.TaskManagementSystem.Entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    // Chỉ thêm trường tên Role, không bê nguyên object Role vào
    private String roleName; 
}