package com.example.victorymoments.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public ForbiddenException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
