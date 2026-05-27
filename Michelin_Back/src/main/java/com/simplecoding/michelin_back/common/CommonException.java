package com.simplecoding.michelin_back.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CommonException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public CommonException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    // 자주 쓰는 예외 정적 메서드
    public static CommonException notFound(String message) {
        return new CommonException(HttpStatus.NOT_FOUND, message);
    }

    public static CommonException badRequest(String message) {
        return new CommonException(HttpStatus.BAD_REQUEST, message);
    }

    public static CommonException forbidden(String message) {
        return new CommonException(HttpStatus.FORBIDDEN, message);
    }

    public static CommonException unauthorized(String message) {
        return new CommonException(HttpStatus.UNAUTHORIZED, message);
    }
}