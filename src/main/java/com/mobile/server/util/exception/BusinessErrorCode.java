package com.mobile.server.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrorCode {
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 파일 타입을 요청했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    FORMAT_MISMATCH(HttpStatus.BAD_REQUEST, "타입 오류 혹은 JSON 형식 에러가 발생했습니다. "),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "유효하지 않은 파라미터가 포함되어 있습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "타입 오류 발생가 발생했습니다. ");


    private final HttpStatus status;
    private final String message;

    BusinessErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }


}
