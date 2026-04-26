package com.caffzzzip.common.exception;

import com.caffzzzip.common.error.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;  // 사용자 정의 메시지

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage); // Exception 메시지는 customMessage
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
}
