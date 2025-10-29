package com.mobile.server.util.exception;


import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException e, WebRequest request) {
        log.info("BusinessException Response", e);
        BusinessErrorCode errorCode = e.getErrorCode();
        return createResponse(errorCode.getStatus(), errorCode.getMessage(), null, request);
    }

    // 처리할 수 없는 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUncaughtException(Exception e, WebRequest request) {
        log.warn("Uncaught Exception Response", e);
        BusinessErrorCode errorCode = BusinessErrorCode.INTERNAL_SERVER_ERROR;
        return createResponse(errorCode.getStatus(), errorCode.getMessage(), null, request);
    }


    //body -> DTO 변환 예외(타입 매핑 불가, json 문법 오류 등)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        log.warn("BodyMessageNotReadable Exception Response", e);
        BusinessErrorCode errorCode = BusinessErrorCode.FORMAT_MISMATCH;
        ProblemDetail body = createProblemDetail(errorCode.getStatus(), errorCode.getMessage(), null, request);
        return super.handleExceptionInternal(e, body, headers, errorCode.getStatus(), request);
    }

    // 유효성 검증 예외
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        log.warn("MethodArgumentNotValid Exception Response", e);
        BusinessErrorCode errorCode = BusinessErrorCode.INVALID_PARAMETER;
        Map<String, Object> fieldErrors = e.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (oldValue, newValue) -> oldValue
                ));
        ProblemDetail body = createProblemDetail(errorCode.getStatus(), errorCode.getMessage(), fieldErrors,
                request);
        return super.handleExceptionInternal(e, body, headers, errorCode.getStatus(), request);
    }

    // 파라미터 타입 예외 (@RequestParam, @PathVariable)
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException e, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        log.warn("TypeMismatch Exception", e);
        BusinessErrorCode errorCode = BusinessErrorCode.TYPE_MISMATCH;
        String requiredType = (e.getRequiredType() != null) ? e.getRequiredType().getSimpleName() : "알 수 없음";
        Map<String, Object> properties = Map.of(
                "parameter", e.getPropertyName(),
                "requiredType", requiredType
        );
        ProblemDetail body = createProblemDetail(errorCode.getStatus(), errorCode.getMessage(), properties,
                request);
        return super.handleExceptionInternal(e, body, headers, errorCode.getStatus(), request);
    }

    private ProblemDetail createProblemDetail(HttpStatusCode status, String message, Map<String, Object> properties,
                                              WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        setInstance(problemDetail, request);
        if (properties != null) {
            problemDetail.setProperties(properties);
        }
        return problemDetail;
    }

    //사용자 정의 예외 응답
    private ResponseEntity<ProblemDetail> createResponse(HttpStatusCode status, String message,
                                                         Map<String, Object> properties, WebRequest request) {
        ProblemDetail body = createProblemDetail(status, message, properties, request);
        return ResponseEntity.status(status).body(body);
    }


    // URI 설정 메서드
    private void setInstance(ProblemDetail problemDetail, WebRequest request) {
        String requestURI = ((ServletWebRequest) request).getRequest().getRequestURI();
        problemDetail.setInstance(URI.create(requestURI));
    }
}
