package com.resumepilot.common.result;

import lombok.Getter;

/**
 * 统一状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ========== 业务状态码 ==========
    RESUME_PARSE_ERROR(1001, "简历解析失败"),
    RESUME_NOT_FOUND(1002, "简历不存在"),
    ATS_ANALYSIS_ERROR(1003, "ATS 分析失败"),
    JD_MATCH_ERROR(1004, "JD 匹配失败"),
    AI_SERVICE_ERROR(1005, "AI 服务调用失败"),
    FILE_UPLOAD_ERROR(1006, "文件上传失败"),
    FILE_FORMAT_ERROR(1007, "文件格式不支持");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
