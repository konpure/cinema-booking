-- 可选：重置数据库并重建（会清空所有订单数据）
-- 用法：mysql -u root -p < docs/init-local-db.sql

DROP DATABASE IF EXISTS cinema;
CREATE DATABASE cinema DEFAULT CHARACTER SET utf8mb4;

-- 重启后端后会自动执行 schema.sql 和 DataInitializer 填充演示数据
