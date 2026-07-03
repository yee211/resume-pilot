package com.resumepilot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * pgvector 类型注册配置
 *
 * 问题：PostgreSQL JDBC 驱动默认不认识 vector 类型
 * 解决：用 SQL 向 pg_catalog 注册 vector 的 OID 映射
 *
 * 原理：告诉 JDBC 驱动 pg_type 里 vector 类型的 OID 是多少
 * 这样驱动就能正确序列化/反序列化 vector 数据了
 */
@Configuration
public class PgVectorConfig {

    @Bean
    public PgVectorInitializer pgVectorInitializer(DataSource dataSource) {
        return new PgVectorInitializer(dataSource);
    }

    static class PgVectorInitializer {

        PgVectorInitializer(DataSource dataSource) {
            Connection conn = null;
            try {
                conn = DataSourceUtils.getConnection(dataSource);
                try (Statement stmt = conn.createStatement()) {
                    // 确保 vector 扩展存在
                    stmt.execute("CREATE EXTENSION IF NOT EXISTS vector");

                    // 查出 vector 类型的 OID，注册到 JDBC 驱动
                    var rs = stmt.executeQuery(
                            "SELECT oid FROM pg_type WHERE typname = 'vector'"
                    );
                    if (rs.next()) {
                        int oid = rs.getInt(1);
                        // 注册 vector 类型：告诉 JDBC 驱动这个 OID 对应 PGobject
                        conn.getTypeMap().put("vector", org.postgresql.util.PGobject.class);
                    }
                }
            } catch (Exception e) {
                // 忽略，可能已经注册过
            } finally {
                if (conn != null) {
                    DataSourceUtils.releaseConnection(conn, dataSource);
                }
            }
        }
    }
}
