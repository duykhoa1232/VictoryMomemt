package com.example.victorymoments.exception;

public class NotFoundException extends BaseException {
    public NotFoundException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public NotFoundException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
