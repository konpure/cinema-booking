package com.cinema.config.datasource;

import com.cinema.annotation.ReadOnly;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * AOP 切面 — 根据 @ReadOnly 注解自动切换数据源
 * Order=1 确保在 @Transactional 之前执行（事务需要先确定数据源）
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    @Before("@annotation(readOnly)")
    public void switchToSlave(ReadOnly readOnly) {
        log.debug("Switch to SLAVE datasource");
        DataSourceContextHolder.set(DataSourceContextHolder.SLAVE);
    }

    @After("@annotation(readOnly)")
    public void clear(ReadOnly readOnly) {
        DataSourceContextHolder.clear();
    }
}
