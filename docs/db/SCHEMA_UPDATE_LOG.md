# 数据库架构更新日志

## [2026-02-02] - 修复 emp_profile 表缺失字段

### 问题描述
Dashboard 页面加载近期入职纪念时报错：`Unknown column 'quit_date' in 'field list'`。
原因是 `emp_profile` 表结构与 `EmpProfile.java` 实体类不一致，缺少多个字段。

### 变更内容
在 `emp_profile` 表中添加了以下缺失字段：
- `post_id`: 岗位ID (BIGINT)
- `level_id`: 职级ID (BIGINT)
- `resume_url`: 简历链接 (VARCHAR(255))
- `quit_date`: 离职日期 (DATE)
- `quit_reason`: 离职原因 (VARCHAR(500))

### 执行脚本
```sql
ALTER TABLE `emp_profile` 
ADD COLUMN `post_id` bigint(20) DEFAULT NULL COMMENT '岗位ID',
ADD COLUMN `level_id` bigint(20) DEFAULT NULL COMMENT '职级ID',
ADD COLUMN `resume_url` varchar(255) DEFAULT NULL COMMENT '简历链接',
ADD COLUMN `quit_date` date DEFAULT NULL COMMENT '离职日期',
ADD COLUMN `quit_reason` varchar(500) DEFAULT NULL COMMENT '离职原因';
```

### 文档更新
- 已同步更新 `init_v2.sql`
- 已同步更新 `docs/db/schema.sql`
