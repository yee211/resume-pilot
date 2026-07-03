package com.resumepilot.resume.vo;

import com.resumepilot.resume.dto.ParsedResumeDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历视图对象（View Object）
 * 专门用来返回给前端，不暴露数据库内部细节
 *
 * DTO vs VO 的区别：
 *   DTO = 前端传给后端的数据（入参）
 *   VO  = 后端返回给前端的数据（出参）
 *   Entity = 数据库的映射（绝不直接返回给前端）
 */
@Data
public class ResumeVO {

    private Long id;
    private String fileName;
    private Integer status;
    private ParsedResumeDTO parsedData;
    private LocalDateTime createdAt;
}
