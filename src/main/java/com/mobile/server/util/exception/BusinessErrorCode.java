package com.mobile.server.util.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrorCode {
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "잘못된 파일 타입을 요청했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    FORMAT_MISMATCH(HttpStatus.BAD_REQUEST, "타입 오류 혹은 JSON 형식 에러가 발생했습니다. "),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "유효하지 않은 파라미터가 포함되어 있습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "타입 오류 발생가 발생했습니다. "),
    FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "현재 파일과 관련하여 서버에 문제가 있습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),
    FILENAME_MISSING(HttpStatus.BAD_REQUEST, "파일명이 존재하지 않습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "파일의 허용 용량을 초과했습니다"),
    FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    URL_FORBIDDEN(HttpStatus.FORBIDDEN, "사용자는 해당 기능을 사용할 수 없습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "요청하신 카테고리는 존재하지 않습니다"),
    MISSION_NOT_FOUND(HttpStatus.BAD_REQUEST, "요청하신 미션은 존재하지 않습니다.");


    private final HttpStatus status;
    private final String message;

    BusinessErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }


}
