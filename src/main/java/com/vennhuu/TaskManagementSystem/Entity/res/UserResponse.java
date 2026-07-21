package com.vennhuu.TaskManagementSystem.Entity.res;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id ;
    private String name ;
    private String email ;
    private Instant createdAt ;

    private RoleUser role ;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleUser {
        private Long id ;
        private String name ;
    }

}
