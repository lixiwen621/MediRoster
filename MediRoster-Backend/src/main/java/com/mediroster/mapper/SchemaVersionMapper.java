package com.mediroster.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 数据库连通性探测（可用于后续扩展版本表查询）。
 *
 * @author tongguo.li
 */
@Mapper
public interface SchemaVersionMapper {

    /**
     * 轻量 ping，兼容 MySQL 与 H2（测试 profile）。
     *
     * @return 固定为 1
     */
    @Select("SELECT 1")
    int ping();
}
