-- 招聘管理模块表结构设计

-- 1. 职位需求表
CREATE TABLE IF NOT EXISTS `rec_job` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `job_title` VARCHAR(100) NOT NULL COMMENT '职位名称',
    `dept_id` BIGINT NOT NULL COMMENT '所属部门',
    `head_count` INT NOT NULL DEFAULT 1 COMMENT '招聘人数',
    `job_desc` TEXT COMMENT '职位描述',
    `requirements` TEXT COMMENT '任职要求',
    `salary_range` VARCHAR(50) COMMENT '薪资范围',
    `location` VARCHAR(100) COMMENT '工作地点',
    `status` TINYINT DEFAULT 1 COMMENT '1开启 0关闭',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '发布人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘职位表';

-- 2. 候选人简历表
CREATE TABLE IF NOT EXISTS `rec_candidate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `education` VARCHAR(50) COMMENT '学历',
    `experience_years` INT COMMENT '工作年限',
    `resume_url` VARCHAR(255) COMMENT '简历文件地址',
    `apply_job_id` BIGINT NOT NULL COMMENT '应聘职位ID',
    `status` TINYINT DEFAULT 1 COMMENT '1新简历 2初筛通过 3面试中 4已发Offer 5已入职 9淘汰',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人简历表';

-- 3. 面试记录表
CREATE TABLE IF NOT EXISTS `rec_interview` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `candidate_id` BIGINT NOT NULL COMMENT '候选人ID',
    `interviewer_id` BIGINT COMMENT '面试官用户ID',
    `interview_time` DATETIME NOT NULL COMMENT '面试时间',
    `type` TINYINT DEFAULT 1 COMMENT '1初试 2复试 3终试',
    `link_url` VARCHAR(255) COMMENT '在线会议链接',
    `score` INT COMMENT '面试评分(0-100)',
    `comment` TEXT COMMENT '面试评价',
    `result` TINYINT DEFAULT 0 COMMENT '0待反馈 1通过 2不通过',
    `status` TINYINT DEFAULT 0 COMMENT '0待面试 1已完成 2已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试安排表';

-- 插入一些演示数据 - 职位
INSERT INTO `rec_job` (`job_title`, `dept_id`, `head_count`, `job_desc`, `status`) 
VALUES ('高级 Java 开发工程师', 1, 3, '负责核心业务系统的后端开发与架构优化。', 1);

INSERT INTO `rec_job` (`job_title`, `dept_id`, `head_count`, `job_desc`, `status`) 
VALUES ('Vue3 前端专家', 1, 2, '负责公司玻璃拟态 UI 组件库的研发。', 1);

-- 插入演示数据 - 候选人
INSERT INTO `rec_candidate` (`name`, `phone`, `email`, `education`, `experience_years`, `apply_job_id`, `status`) 
VALUES ('李雷', '13800138000', 'lilei@example.com', '本科', 3, 1, 1);

INSERT INTO `rec_candidate` (`name`, `phone`, `email`, `education`, `experience_years`, `apply_job_id`, `status`) 
VALUES ('韩梅梅', '13900139000', 'hanmeimei@example.com', '硕士', 5, 1, 2);
