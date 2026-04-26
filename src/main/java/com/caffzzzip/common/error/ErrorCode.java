package com.caffzzzip.common.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

    // 400 BAD REQUEST
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "유효성 검사에 실패하였습니다.", "BAD_REQUEST_400"),
    ALREADY_EXISTS_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다.", "BAD_REQUEST_400"),

    // 401 UNAUTHORIZED
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "JWT가 비어있거나 잘못된 값입니다.", "UNAUTHORIZED_401"),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT가 만료되었습니다.", "UNAUTHORIZED_401"),
    JWT_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "지원하지 않는 JWT 형식입니다.", "UNAUTHORIZED_401"),
    JWT_MALFORMED(HttpStatus.UNAUTHORIZED, "잘못된 JWT 토큰입니다.", "UNAUTHORIZED_401"),
    JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "JWT 서명 검증에 실패했습니다.", "UNAUTHORIZED_401"),
    JWT_VALIDATION_FAILED(HttpStatus.UNAUTHORIZED, "JWT 검증에 실패했습니다.", "UNAUTHORIZED_401"),
    NO_AUTHORIZATION_EXCEPTION(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다.", "UNAUTHORIZED_401"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.", "UNAUTHORIZED_401"),

    // 403 FORBIDDEN
    FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", "FORBIDDEN_403"),

    // 404 NOT FOUND
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자가 없습니다.", "NOT_FOUND_404"),
    LIKE_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 좋아요가 없습니다.", "NOT_FOUND_404"),
    COMMENT_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 댓글이 없습니다.", "NOT_FOUND_404"),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다", "INTERNAL_SERVER_ERROR_500");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }

}

