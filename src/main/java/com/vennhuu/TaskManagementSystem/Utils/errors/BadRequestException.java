package com.vennhuu.TaskManagementSystem.Utils.errors;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
