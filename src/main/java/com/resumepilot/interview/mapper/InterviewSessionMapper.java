package com.resumepilot.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resumepilot.interview.entity.InterviewSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试会话 Mapper
 */
@Mapper
public interface InterviewSessionMapper extends BaseMapper<InterviewSession> {
}
