package com.resumepilot.ats.vo;

import com.resumepilot.ats.dto.AtsResultDTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AtsVO {
    private Long id;
    private Long resumeId;
    private Integer status;
    private AtsResultDTO result;
    private LocalDateTime analyzedAt;
}
