package com.example.victorymoments.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public UnauthorizedException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
