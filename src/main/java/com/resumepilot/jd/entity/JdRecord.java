package com.resumepilot.jd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("jd_record")
public class JdRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("jd_text")
    private String jdText;

    @TableField("match_score")
    private Integer matchScore;

    @TableField("result_json")
    private String resultJson;

    /** 状态：0=待分析  1=分析成功  2=分析失败 */
    @TableField("status")
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
