package com.resumepilot.resume.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resumepilot.resume.entity.Resume;
import org.apache.ibatis.annotations.Mapper;

/**
 * 简历 Mapper
 *
 * 继承 BaseMapper<Resume> 后，自动拥有：
 *   insert(Resume)         → 插入
 *   deleteById(Long)       → 按 ID 删除
 *   updateById(Resume)     → 按 ID 更新（只更新非 null 字段）
 *   selectById(Long)       → 按 ID 查询
 *   selectList(Wrapper)    → 条件查询
 *   selectPage(Page, Wrapper) → 分页查询
 *
 * 这些方法全部由 MyBatis Plus 自动生成，你不需要写任何 XML 或 SQL
 *
 * @Mapper 注解告诉 Spring：这是一个 MyBatis 接口，需要生成代理实现
 */
@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
}
