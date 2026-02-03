CREATE TABLE IF NOT EXISTS `sys_announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `type` VARCHAR(50) DEFAULT 'news' COMMENT '类型(news,notice,event)',
  `is_published` TINYINT DEFAULT 0 COMMENT '发布状态(0草稿 1已发布)',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 2026-02-01: Update SysPayroll
ALTER TABLE sys_payroll ADD COLUMN deduction DECIMAL(10,2) DEFAULT 0.00 AFTER insurance;

-- 2026-02-02: Add SysConfig table for system configurations (e.g. attendance rules)
CREATE TABLE IF NOT EXISTS `sys_config` (
  `config_id` INT NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` VARCHAR(100) DEFAULT '' COMMENT '参数名称',
  `config_key` VARCHAR(100) DEFAULT '' COMMENT '参数键名',
  `config_value` VARCHAR(500) DEFAULT '' COMMENT '参数键值',
  `config_type` CHAR(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- Initialize Attendance Configs if not exist
INSERT IGNORE INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `remark`) VALUES 
('工作日设置', 'sys.attendance.workDays', '1,2,3,4,5', 'Y', '1-7代表周一至周日'),
('上班时间', 'sys.attendance.startTime', '09:00', 'Y', '每日上班打卡时间'),
('下班时间', 'sys.attendance.endTime', '18:00', 'Y', '每日下班打卡时间');

-- 2026-02-02: Job System Tables
CREATE TABLE IF NOT EXISTS `sys_post` (
  `post_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` VARCHAR(64) NOT NULL COMMENT '岗位编码',
  `post_name` VARCHAR(50) NOT NULL COMMENT '岗位名称',
  `post_sort` INT DEFAULT 0 COMMENT '显示顺序',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`post_id`),
  UNIQUE KEY `uk_post_code` (`post_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位信息表';

CREATE TABLE IF NOT EXISTS `sys_job_level` (
  `level_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '职级ID',
  `level_code` VARCHAR(64) NOT NULL COMMENT '职级编码',
  `level_name` VARCHAR(50) NOT NULL COMMENT '职级名称',
  `level_sort` INT DEFAULT 0 COMMENT '显示顺序',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `min_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '最低薪资',
  `max_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '最高薪资',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`level_id`),
  UNIQUE KEY `uk_level_code` (`level_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职级信息表';

-- 增加员工档案中的岗位和职级关联列
ALTER TABLE `emp_profile` ADD COLUMN `post_id` BIGINT DEFAULT NULL COMMENT '岗位ID' AFTER `job_title`;
ALTER TABLE `emp_profile` ADD COLUMN `level_id` BIGINT DEFAULT NULL COMMENT '职级ID' AFTER `post_id`;

-- 2026-02-02: Onboarding Funnel Tables
CREATE TABLE IF NOT EXISTS `sys_candidate` (
  `candidate_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '候选人ID',
  `full_name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `post_id` BIGINT DEFAULT NULL COMMENT '应聘岗位',
  `resume_url` VARCHAR(255) DEFAULT NULL COMMENT '简历地址',
  `source` VARCHAR(50) DEFAULT 'other' COMMENT '渠道(招聘平台、内推等)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(1初筛 2面试中 3待入职 4已拒绝 5已入职)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人表';

CREATE TABLE IF NOT EXISTS `sys_interview` (
  `interview_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '面试ID',
  `candidate_id` BIGINT NOT NULL COMMENT '候选人ID',
  `interviewer_id` BIGINT DEFAULT NULL COMMENT '面试官ID',
  `interview_time` DATETIME DEFAULT NULL COMMENT '面试时间',
  `round` INT DEFAULT 1 COMMENT '面试轮次',
  `score` INT DEFAULT NULL COMMENT '评分',
  `comment` TEXT COMMENT '评价',
  `status` TINYINT DEFAULT 1 COMMENT '面试状态(1待面试 2通过 3拒绝)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试记录表';

-- 增加简历地址列
ALTER TABLE `emp_profile` ADD COLUMN `resume_url` VARCHAR(255) DEFAULT NULL COMMENT '简历下载地址' AFTER `level_id`;

-- 2026-02-02: Performance Appraisal Tables
CREATE TABLE IF NOT EXISTS `sys_performance_cycle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '周期ID',
  `cycle_name` VARCHAR(100) NOT NULL COMMENT '周期名称(如：2024Q1)',
  `start_date` DATE DEFAULT NULL COMMENT '开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '结束日期',
  `status` TINYINT DEFAULT 1 COMMENT '状态(1草稿 2进行中 3已归档)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效评价周期';

CREATE TABLE IF NOT EXISTS `sys_performance_appraisal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分记录ID',
  `cycle_id` BIGINT NOT NULL COMMENT '周期ID',
  `emp_id` BIGINT NOT NULL COMMENT '员工ID',
  `score` DECIMAL(5,2) DEFAULT 0 COMMENT '最终得分',
  `rating` VARCHAR(10) DEFAULT '' COMMENT '评级(A,B,C,D)',
  `potential_score` INT DEFAULT 3 COMMENT '潜力评分(1-5, 用于9宫格)',
  `comment` TEXT COMMENT '评价内容',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '评价人ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态(1待评 2已评价)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效评分明细';

-- 2026-02-02: Audit Log Table
CREATE TABLE IF NOT EXISTS `sys_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `title` VARCHAR(100) DEFAULT '' COMMENT '模块标题',
  `business_type` INT DEFAULT 0 COMMENT '业务类型(0其它 1新增 2修改 3删除)',
  `method` VARCHAR(255) DEFAULT '' COMMENT '方法名称',
  `request_method` VARCHAR(10) DEFAULT '' COMMENT '请求方式',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人员ID',
  `operator_name` VARCHAR(50) DEFAULT '' COMMENT '操作人员姓名',
  `oper_url` VARCHAR(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` VARCHAR(128) DEFAULT '' COMMENT '主机地址',
  `oper_param` TEXT COMMENT '请求参数',
  `json_result` TEXT COMMENT '返回参数',
  `status` TINYINT DEFAULT 1 COMMENT '操作状态(1正常 0异常)',
  `error_msg` TEXT COMMENT '错误消息',
  `oper_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- 2026-02-02: Document Center Tables
CREATE TABLE IF NOT EXISTS `sys_doc_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `order_num` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分类表';

CREATE TABLE IF NOT EXISTS `sys_document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `content` LONGTEXT COMMENT '内容/正文',
  `summary` VARCHAR(500) DEFAULT '' COMMENT '摘要',
  `category_id` BIGINT DEFAULT NULL COMMENT '所属分类',
  `doc_type` VARCHAR(20) DEFAULT 'article' COMMENT '类型(article, file)',
  `file_url` VARCHAR(255) DEFAULT '' COMMENT '附件地址',
  `view_count` INT DEFAULT 0 COMMENT '查看次数',
  `is_published` TINYINT DEFAULT 1 COMMENT '发布状态(0草稿 1已发布)',
  `create_by` VARCHAR(50) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';



-- 2026-02-04: MFA (Two-Step Verification)
ALTER TABLE `sys_user` ADD COLUMN `mfa_secret` VARCHAR(64) DEFAULT NULL COMMENT 'MFA密钥' AFTER `remark`;
ALTER TABLE `sys_user` ADD COLUMN `mfa_enabled` TINYINT DEFAULT 0 COMMENT '是否开启MFA' AFTER `mfa_secret`;
