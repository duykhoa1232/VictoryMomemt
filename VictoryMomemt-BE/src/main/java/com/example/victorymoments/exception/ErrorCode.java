package com.example.victorymoments.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ==================== TOKEN ERRORS ====================
    INVALID_REFRESH_TOKEN("token.invalid.refresh", "INVALID_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("token.refresh.expired", "REFRESH_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED),

    // ==================== POST ERRORS ====================
    POST_IMAGES_MAX("post.images.max", "POST_IMAGES_MAX", HttpStatus.BAD_REQUEST),
    POST_VIDEOS_MAX("post.videos.max", "POST_VIDEOS_MAX", HttpStatus.BAD_REQUEST),
    POST_AUDIOS_MAX("post.audios.max", "POST_AUDIOS_MAX", HttpStatus.BAD_REQUEST),
    POST_INVALID_PRIVACY_STATUS("post.invalid.privacy.status", "POST_INVALID_PRIVACY_STATUS", HttpStatus.BAD_REQUEST),
    POST_INACTIVE_COMMENT("post.inactive.comment", "POST_INACTIVE_COMMENT", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND("post.notfound", "POST_NOT_FOUND", HttpStatus.NOT_FOUND),
    POST_CONTENT_REQUIRED("post.content.required", "POST_CONTENT_REQUIRED", HttpStatus.BAD_REQUEST),
    POST_UNAUTHORIZED_ACTION("post.unauthorized.action", "POST_UNAUTHORIZED_ACTION", HttpStatus.FORBIDDEN),
    POST_UNAUTHORIZED_DELETE("post.unauthorized.delete", "POST_UNAUTHORIZED_DELETE", HttpStatus.FORBIDDEN),
    POST_UNAUTHORIZED_UPDATE("post.unauthorized.update", "POST_UNAUTHORIZED_UPDATE", HttpStatus.FORBIDDEN),
    POST_INVALID_MEDIA_TYPE("post.invalid.media.type", "POST_INVALID_MEDIA_TYPE", HttpStatus.BAD_REQUEST),
    POST_LIKE_ALREADY("post.like.already", "POST_LIKE_ALREADY", HttpStatus.BAD_REQUEST),
    POST_LIKE_NOT_FOUND("post.like.notfound", "POST_LIKE_NOT_FOUND", HttpStatus.NOT_FOUND),
    POST_INACTIVE_ACTION("post.inactive.action", "POST_INACTIVE_ACTION", HttpStatus.BAD_REQUEST),
    POST_ACCESS_DENIED("post.access.denied", "POST_ACCESS_DENIED", HttpStatus.FORBIDDEN),
    POST_UPDATE_INVALID_DELETED_URLS("post.update.invalid.deleted.urls", "POST_UPDATE_INVALID_DELETED_URLS", HttpStatus.BAD_REQUEST),
    POST_DELETE_S3_FAILED("post.delete.s3.failed", "POST_DELETE_S3_FAILED", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==================== COMMENT ERRORS ====================
    COMMENT_NOT_FOUND("comment.notfound", "COMMENT_NOT_FOUND", HttpStatus.NOT_FOUND),
    COMMENT_CONTENT_REQUIRED("comment.content.required", "COMMENT_CONTENT_REQUIRED", HttpStatus.BAD_REQUEST),
    COMMENT_UNAUTHORIZED_DELETE("comment.unauthorized.delete", "COMMENT_UNAUTHORIZED_DELETE", HttpStatus.FORBIDDEN),
    COMMENT_UNAUTHORIZED_UPDATE("comment.unauthorized.update", "COMMENT_UNAUTHORIZED_UPDATE", HttpStatus.FORBIDDEN),
    COMMENT_POST_NOT_FOUND("comment.post.notfound", "COMMENT_POST_NOT_FOUND", HttpStatus.NOT_FOUND),
    COMMENT_INACTIVE_COMMENT("comment.inactive.comment", "COMMENT_INACTIVE_COMMENT", HttpStatus.BAD_REQUEST),
    COMMENT_REPLY_POST_MISMATCH("comment.reply.post.mismatch", "COMMENT_REPLY_POST_MISMATCH", HttpStatus.BAD_REQUEST),

    // ==================== FRIENDSHIP ERRORS ====================
    FRIENDSHIP_REQUEST_SELF("friendship.request.self", "FRIENDSHIP_REQUEST_SELF", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_REQUEST_PENDING("friendship.request.pending", "FRIENDSHIP_REQUEST_PENDING", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_REQUEST_ALREADY_PENDING("friendship.request.already.pending", "FRIENDSHIP_REQUEST_ALREADY_PENDING", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_REQUEST_PENDING_FROM_OTHER("friendship.request.pending.from.other", "FRIENDSHIP_REQUEST_PENDING_FROM_OTHER", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_ALREADY_FRIENDS("friendship.already.friends", "FRIENDSHIP_ALREADY_FRIENDS", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_REQUEST_NOT_FOUND("friendship.request.notfound", "FRIENDSHIP_REQUEST_NOT_FOUND", HttpStatus.NOT_FOUND),
    FRIENDSHIP_NOT_FRIENDS("friendship.not.friends", "FRIENDSHIP_NOT_FRIENDS", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_INVALID_STATUS("friendship.invalid.status", "FRIENDSHIP_INVALID_STATUS", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_ALREADY_BLOCKED("friendship.already.blocked", "FRIENDSHIP_ALREADY_BLOCKED", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_NOT_BLOCKED("friendship.not.blocked", "FRIENDSHIP_NOT_BLOCKED", HttpStatus.BAD_REQUEST),
    FRIENDSHIP_UNAUTHORIZED_ACCEPT("friendship.unauthorized.accept", "FRIENDSHIP_UNAUTHORIZED_ACCEPT", HttpStatus.FORBIDDEN),
    FRIENDSHIP_UNAUTHORIZED_DECLINE("friendship.unauthorized.decline", "FRIENDSHIP_UNAUTHORIZED_DECLINE", HttpStatus.FORBIDDEN),
    FRIENDSHIP_REMOVE_NOT_FOUND("friendship.remove.notfound", "FRIENDSHIP_REMOVE_NOT_FOUND", HttpStatus.NOT_FOUND),

    // ==================== SHARE ERRORS ====================
    SHARE_NOT_FOUND("share.notfound", "SHARE_NOT_FOUND", HttpStatus.NOT_FOUND),
    SHARE_ALREADY_SHARED("share.already.shared", "SHARE_ALREADY_SHARED", HttpStatus.BAD_REQUEST),
    SHARE_UNAUTHORIZED_ACTION("share.unauthorized.action", "SHARE_UNAUTHORIZED_ACTION", HttpStatus.FORBIDDEN),
    FORBIDDEN_ACTION("common.forbidden.action", "FORBIDDEN_ACTION", HttpStatus.FORBIDDEN),

    // ==================== AUTH ERRORS ====================
    AUTH_EMAIL_REQUIRED("auth.email.required", "AUTH_EMAIL_REQUIRED", HttpStatus.BAD_REQUEST),
    AUTH_EMAIL_SIZE("auth.email.size", "AUTH_EMAIL_SIZE", HttpStatus.BAD_REQUEST),
    AUTH_PASSWORD_REQUIRED("auth.password.required", "AUTH_PASSWORD_REQUIRED", HttpStatus.BAD_REQUEST),
    AUTH_PASSWORD_SIZE("auth.password.size", "AUTH_PASSWORD_SIZE", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTS("email.is.exists", "EMAIL_EXISTS", HttpStatus.BAD_REQUEST),
    VERIFY_TOKEN_INVALID("verification.token.invalid", "VERIFY_TOKEN_INVALID", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED("email.send.failed", "EMAIL_SEND_FAILED", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTH_INVALID("auth.invalid", "AUTH_INVALID", HttpStatus.UNAUTHORIZED),
    AUTH_MISSING_FIELDS("auth.missing.fields", "AUTH_MISSING_FIELDS", HttpStatus.BAD_REQUEST),

    // ==================== USER PROFILE ERRORS ====================
    USER_NOT_FOUND("user.notfound", "USER_NOT_FOUND", HttpStatus.NOT_FOUND),
    PROFILE_NOT_FOUND("profile.notfound", "PROFILE_NOT_FOUND", HttpStatus.NOT_FOUND),
    USER_PHONE_REQUIRED("user.phone.required", "USER_PHONE_REQUIRED", HttpStatus.BAD_REQUEST),
    USER_PHONE_INVALID("user.phone.invalid", "USER_PHONE_INVALID", HttpStatus.BAD_REQUEST),
    USER_PHONE_EXISTS("user.phone.exists", "USER_PHONE_EXISTS", HttpStatus.BAD_REQUEST),
    USER_INVALID_ID("user.invalid.id", "USER_INVALID_ID", HttpStatus.BAD_REQUEST),
    USER_NOT_UNIQUE_FIELD("user.not.unique.field", "USER_NOT_UNIQUE_FIELD", HttpStatus.BAD_REQUEST),
    USER_PROFILE_UNAUTHORIZED_UPDATE("user.profile.update.unauthorized", "USER_PROFILE_UNAUTHORIZED_UPDATE", HttpStatus.FORBIDDEN),

    // ==================== COMMON ERRORS ====================
    ERROR_UNKNOWN("error.unknown", "ERROR_UNKNOWN", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED("validation.failed", "VALIDATION_FAILED", HttpStatus.BAD_REQUEST),

    // ==================== FRONTEND POST/COMMENT SUCCESS ====================
    COMMENT_CREATE_SUCCESS("comment.create.success", "COMMENT_CREATE_SUCCESS", HttpStatus.OK),
    COMMENT_UPDATE_SUCCESS("comment.update.success", "COMMENT_UPDATE_SUCCESS", HttpStatus.OK),
    COMMENT_DELETE_SUCCESS("comment.delete.success", "COMMENT_DELETE_SUCCESS", HttpStatus.OK),
    POST_CREATE_SUCCESS("post.created.success", "POST_CREATE_SUCCESS", HttpStatus.OK),
    POST_UPDATE_SUCCESS("post.updated.success", "POST_UPDATE_SUCCESS", HttpStatus.OK),
    POST_DELETE_SUCCESS("post.deleted.success", "POST_DELETE_SUCCESS", HttpStatus.OK),
    ;

    private final String messageKey;
    private final String code;
    private final HttpStatus httpStatus;

    ErrorCode(String messageKey, String code, HttpStatus httpStatus) {
        this.messageKey = messageKey;
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
