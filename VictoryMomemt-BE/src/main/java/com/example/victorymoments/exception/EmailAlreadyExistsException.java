package com.example.victorymoments.exception;

public class EmailAlreadyExistsException extends BadRequestException {
    public EmailAlreadyExistsException(Object... args) {
        super(ErrorCode.EMAIL_EXISTS, args);
    }
}
