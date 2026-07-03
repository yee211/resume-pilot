package com.resumepilot.jd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resumepilot.jd.entity.JdRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JdMapper extends BaseMapper<JdRecord> {
}
