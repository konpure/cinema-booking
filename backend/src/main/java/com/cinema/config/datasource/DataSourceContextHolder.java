package com.cinema.config.datasource;

/**
 * 数据源上下文持有者 — ThreadLocal 存储当前线程应使用的数据源 key
 */
public class DataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static final String MASTER = "master";
    public static final String SLAVE  = "slave";

    public static void set(String key) {
        CONTEXT.set(key);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
