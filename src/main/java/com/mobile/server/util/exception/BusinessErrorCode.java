package com.mobile.server.util.exception;

import org.springframework.http.HttpStatus;

public enum BusinessErrorCode {
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 파일 타입을 요청했습니다.");

    private final HttpStatus status;
    private final String message;

    BusinessErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
