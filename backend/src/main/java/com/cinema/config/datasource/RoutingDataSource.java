package com.cinema.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态路由数据源 — 根据 DataSourceContextHolder 中的 key 切换主/从库
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String key = DataSourceContextHolder.get();
        // 默认走主库（没有设置 key 时）
        return key != null ? key : DataSourceContextHolder.MASTER;
    }
}
