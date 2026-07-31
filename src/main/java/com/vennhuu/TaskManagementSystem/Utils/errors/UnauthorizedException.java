package com.vennhuu.TaskManagementSystem.Utils.errors;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
