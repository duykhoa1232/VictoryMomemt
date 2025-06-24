package com.example.victorymoments.exception;

public class BadRequestException extends BaseException {
    public BadRequestException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public BadRequestException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
