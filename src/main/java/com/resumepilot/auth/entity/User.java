package com.resumepilot.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 密码字段用 BCrypt 加密存储（不可逆）
 */
@Data
@TableName("\"user\"")  // user 是 PostgreSQL 保留字，必须加双引号转义
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("email")
    private String email;

    @TableField("password")
    private String password;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
