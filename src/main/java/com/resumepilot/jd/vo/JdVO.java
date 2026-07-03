package com.resumepilot.jd.vo;

import com.resumepilot.jd.dto.JdResultDTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JdVO {
    private Long id;
    private Long resumeId;
    private String jdText;
    private Integer status;
    private JdResultDTO result;
    private LocalDateTime createdAt;
}
