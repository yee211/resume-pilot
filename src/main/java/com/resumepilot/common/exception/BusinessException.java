package com.resumepilot.common.exception;

import com.resumepilot.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * Service 层抛出，GlobalExceptionHandler 统一捕获
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
