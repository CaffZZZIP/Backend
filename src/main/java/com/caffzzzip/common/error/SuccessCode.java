package com.caffzzzip.common.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessCode {

    // 200 OK
    GET_SUCCESS(HttpStatus.OK, "성공적으로 조회했습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인에 성공하였습니다."),
    MEMBER_DELETE_SUCCESS(HttpStatus.OK, "사용자가 성공적으로 삭제되었습니다."),
    LIKE_DELETE_SUCCESS(HttpStatus.OK, "좋아요가 성공적으로 삭제되었습니다."),
    COMMENT_DELETE_SUCCESS(HttpStatus.OK, "댓글이 성공적으로 삭제되었습니다."),
    UPDATE_SUCCESS(HttpStatus.OK, "성공적으로 수정되었습니다."),

    // 201 CREATED
    EMAIL_SEND_SUCCESS(HttpStatus.CREATED, "이메일 전송에 성공하였습니다."),
    REFRESH_TOKEN_SUCCESS(HttpStatus.CREATED, "리프레시 토큰으로 액세스 토큰 재발급에 성공하였습니다."),
    MEMBER_JOIN_SUCCESS(HttpStatus.CREATED, "회원가입에 성공하였습니다."),  // 새로 추가
    MEMBER_LOGIN_SUCCESS(HttpStatus.CREATED, "로그인에 성공하였습니다."),    // 기존에 있음
    LIKE_SAVE_SUCCESS(HttpStatus.CREATED, "좋아요가 성공적으로 등록되었습니다."),
    COMMENT_SAVE_SUCCESS(HttpStatus.CREATED, "댓글이 성공적으로 등록되었습니다."),
    SAVE_SUCCESS(HttpStatus.CREATED, "성공적으로 저장되었습니다.");


    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
