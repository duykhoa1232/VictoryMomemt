package com.example.victorymoments.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object[] args;
    private final ErrorCode errorEnum;

    /**
     * Constructs a new BaseException using ErrorCode.
     */
    protected BaseException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessageKey());
        this.status = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
        this.args = args;
        this.errorEnum = errorCode;
    }

    /**
     * Constructs a new BaseException using ErrorCode and cause.
     */
    protected BaseException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.getMessageKey(), cause);
        this.status = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
        this.args = args;
        this.errorEnum = errorCode;
    }

    /**
     * Message key for i18n lookup.
     */
    public String getMessageKey() {
        return errorEnum.getMessageKey();
    }
}
