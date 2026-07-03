package com.resumepilot.ats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resumepilot.ats.entity.AtsRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AtsMapper extends BaseMapper<AtsRecord> {
}
