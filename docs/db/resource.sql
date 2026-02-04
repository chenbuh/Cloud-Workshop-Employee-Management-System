-- 行政资源管理模块 SQL 脚本
USE `cloud_ems`;

CREATE TABLE IF NOT EXISTS sys_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '资源名称',
    type VARCHAR(20) DEFAULT 'ROOM' COMMENT '类型 (ROOM:会议室, DEVICE:设备)',
    capacity INT DEFAULT 0 COMMENT '容纳人数',
    location VARCHAR(100) COMMENT '地点',
    facilities VARCHAR(255) COMMENT '设施 (投影仪, 白板等)',
    status TINYINT DEFAULT 1 COMMENT '状态 (1可用 0停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行政资源表';

CREATE TABLE IF NOT EXISTS sys_resource_booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    user_id BIGINT NOT NULL COMMENT '预约人ID',
    title VARCHAR(100) NOT NULL COMMENT '会议标题/用途',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态 (1已预约 0已取消)',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源预约记录表';

-- 预置数据
INSERT INTO sys_resource (name, capacity, location, facilities) VALUES 
('太极殿 (大)', 30, '总部4楼', '投影仪, 音响, 视频会议系统'),
('含光殿 (中)', 12, '总部3楼', '白板, 投影仪'),
('云起台 (小)', 4, '总部3楼', '电视, 连接线');
