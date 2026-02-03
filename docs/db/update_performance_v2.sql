-- 绩效管理模块增强脚本
-- 1. 确保表存在 (如果之前没有手动建表)
CREATE TABLE IF NOT EXISTS `sys_performance_cycle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `cycle_name` VARCHAR(100) NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `status` TINYINT DEFAULT 1 COMMENT '1草稿 2进行中 3已归档',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_performance_appraisal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `cycle_id` BIGINT NOT NULL,
  `emp_id` BIGINT NOT NULL,
  `score` DECIMAL(5,2) DEFAULT 0,
  `rating` VARCHAR(5) COMMENT 'S/A/B/C/D',
  `potential_score` TINYINT DEFAULT 3 COMMENT '1-5',
  `comment` TEXT,
  `reviewer_id` BIGINT DEFAULT NULL,
  `status` TINYINT DEFAULT 1 COMMENT '1待评 2已评价',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 新增字段 (如果不报错说明已存在，报错则说明已存在忽略即可，但在 MySQL 中 ADD COLUMN IF NOT EXISTS 8.0+ 支持)
-- 为了兼容性，这里假设是手动执行，或者使用存储过程，但最简单的是直接运行，如果报错自行调整。
-- 在此模拟第一次添加:

ALTER TABLE `sys_performance_appraisal` 
ADD COLUMN `next_goals` TEXT COMMENT '下一周期目标',
ADD COLUMN `kpi_details` JSON COMMENT '关键绩效指标详情JSON';
