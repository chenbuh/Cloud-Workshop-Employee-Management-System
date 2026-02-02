-- 云工坊员工管理系统 数据库设计 (MySQL 8.0)

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 部门表 (Tree Structure)
-- ----------------------------
CREATE TABLE `sys_dept` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父部门ID',
    `ancestors` VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
    `order_num` INT DEFAULT 0 COMMENT '显示顺序',
    `leader` VARCHAR(20) DEFAULT NULL COMMENT '负责人',
    `phone` VARCHAR(11) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
    `status` CHAR(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ----------------------------
-- 2. 系统用户表
-- ----------------------------
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `user_name` VARCHAR(30) NOT NULL COMMENT '用户账号',
    `nick_name` VARCHAR(30) NOT NULL COMMENT '用户昵称',
    `user_type` VARCHAR(2) DEFAULT '00' COMMENT '用户类型（00系统用户）',
    `email` VARCHAR(50) DEFAULT '' COMMENT '用户邮箱',
    `phonenumber` VARCHAR(11) DEFAULT '' COMMENT '手机号码',
    `sex` CHAR(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
    `avatar` VARCHAR(100) DEFAULT '' COMMENT '头像地址',
    `password` VARCHAR(100) DEFAULT '' COMMENT '密码',
    `status` CHAR(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `login_ip` VARCHAR(128) DEFAULT '' COMMENT '最后登录IP',
    `login_date` DATETIME COMMENT '最后登录时间',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ----------------------------
-- 3. 员工档案表 (核心业务表)
-- ----------------------------
CREATE TABLE `emp_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT DEFAULT NULL COMMENT '绑定的系统用户ID',
    `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `emp_code` VARCHAR(20) NOT NULL COMMENT '工号',
    `full_name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `birthday` DATE DEFAULT NULL COMMENT '出生日期',
    `entry_date` DATE DEFAULT NULL COMMENT '入职日期',
    `probation_end_date` DATE DEFAULT NULL COMMENT '试用期结束日期',
    `job_title` VARCHAR(50) DEFAULT NULL COMMENT '职位名称',
    `post_id` BIGINT DEFAULT NULL COMMENT '岗位ID',
    `level_id` BIGINT DEFAULT NULL COMMENT '职级ID',
    `resume_url` VARCHAR(255) DEFAULT NULL COMMENT '简历链接',
    `work_location` VARCHAR(100) DEFAULT NULL COMMENT '办公地点',
    `education` VARCHAR(20) DEFAULT NULL COMMENT '最高学历',
    `major` VARCHAR(50) DEFAULT NULL COMMENT '专业',
    `emergency_contact` VARCHAR(30) DEFAULT NULL COMMENT '紧急联系人',
    `emergency_phone` VARCHAR(11) DEFAULT NULL COMMENT '紧急联系人电话',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (1在职 2试用 3离职 4退休)',
    `quit_date` DATE DEFAULT NULL COMMENT '离职日期',
    `quit_reason` VARCHAR(500) DEFAULT NULL COMMENT '离职原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_emp_code` (`emp_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工档案表';

-- ----------------------------
-- 4. 员工薪资记录
-- ----------------------------
CREATE TABLE `emp_salary` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `emp_id` BIGINT NOT NULL COMMENT '员工ID',
    `base_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '基本工资',
    `subsidy` DECIMAL(10,2) DEFAULT 0 COMMENT '补贴',
    `bonus` DECIMAL(10,2) DEFAULT 0 COMMENT '奖金',
    `insurance` DECIMAL(10,2) DEFAULT 0 COMMENT '五险一金自付',
    `tax` DECIMAL(10,2) DEFAULT 0 COMMENT '个税',
    `real_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '实发工资',
    `pay_month` VARCHAR(7) NOT NULL COMMENT '发放月份 (YYYY-MM)',
    `status` TINYINT DEFAULT 0 COMMENT '发放状态 (0待发放 1已发放)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工薪资表';

-- ----------------------------
-- 5. 系统公告表
-- ----------------------------
CREATE TABLE `sys_announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `type` VARCHAR(50) DEFAULT 'news' COMMENT '类型(news,notice,event)',
  `is_published` TINYINT DEFAULT 0 COMMENT '发布状态(0草稿 1已发布)',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- ----------------------------
-- 6. 权限与角色 (省略标准中间表，主要定义结构)
-- ----------------------------
-- 包含 sys_role, sys_menu, sys_user_role, sys_role_menu (标准 RBAC)

SET FOREIGN_KEY_CHECKS = 1;
