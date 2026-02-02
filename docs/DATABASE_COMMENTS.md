# 数据库表注释说明

## 已添加的表注释

以下是云工坊员工管理系统所有数据表的注释说明：

| 表名 | 表注释 | 用途说明 |
|------|--------|---------|
| emp_profile | 员工档案信息表 | 存储员工基本档案信息，包括工号、姓名、身份证、入职日期等 |
| emp_salary | 员工薪资记录表 | 记录员工的薪资构成和发放历史 |
| sys_announcement | 系统公告表 | 系统公告发布和管理 |
| sys_attendance | 考勤打卡记录表 | 员工上下班打卡记录 |
| sys_dept | 部门组织架构表 | 企业部门组织架构信息 |
| sys_growth_record | 员工成长记录表 | 记录员工成长轨迹和重要事项 |
| sys_leave | 请假申请记录表 | 员工请假申请和审批记录 |
| sys_menu | 系统菜单权限表 | 系统菜单和权限配置 |
| sys_notification | 系统通知消息表 | 用户系统通知消息 |
| sys_payroll | 薪资发放记录表 | 薪资发放的统计和记录 |
| sys_role | 系统角色表 | 系统角色定义 |
| sys_role_menu | 角色菜单关联表 | 角色与菜单权限的关联关系 |
| sys_salary | 薪资计算表 | 薪资计算相关数据 |
| sys_skill | 员工技能标签表 | 员工技能和专长标签 |
| sys_user | 系统用户账号表 | 系统用户账号信息 |
| sys_user_menu | 用户自定义菜单权限表 | 用户个性化菜单权限配置 |
| sys_user_role | 用户角色关联表 | 用户与角色的关联关系 |

## 表注释的作用

1. **提升可读性**：方便开发人员快速了解表的用途
2. **文档化**：表注释作为数据库设计文档的一部分
3. **工具支持**：很多数据库管理工具会显示表注释，便于查看
4. **团队协作**：新成员能更快理解数据库结构

## SQL 执行记录

```sql
-- 批量添加表注释
ALTER TABLE emp_profile COMMENT '员工档案信息表';
ALTER TABLE emp_salary COMMENT '员工薪资记录表';
ALTER TABLE sys_announcement COMMENT '系统公告表';
ALTER TABLE sys_attendance COMMENT '考勤打卡记录表';
ALTER TABLE sys_dept COMMENT '部门组织架构表';
ALTER TABLE sys_growth_record COMMENT '员工成长记录表';
ALTER TABLE sys_leave COMMENT '请假申请记录表';
ALTER TABLE sys_menu COMMENT '系统菜单权限表';
ALTER TABLE sys_notification COMMENT '系统通知消息表';
ALTER TABLE sys_payroll COMMENT '薪资发放记录表';
ALTER TABLE sys_role COMMENT '系统角色表';
ALTER TABLE sys_role_menu COMMENT '角色菜单关联表';
ALTER TABLE sys_salary COMMENT '薪资计算表';
ALTER TABLE sys_skill COMMENT '员工技能标签表';
ALTER TABLE sys_user COMMENT '系统用户账号表';
ALTER TABLE sys_user_menu COMMENT '用户自定义菜单权限表';
ALTER TABLE sys_user_role COMMENT '用户角色关联表';
```

## 查看表注释

可以使用以下SQL查询查看所有表的注释：

```sql
SELECT 
    TABLE_NAME AS '表名', 
    TABLE_COMMENT AS '表注释' 
FROM 
    INFORMATION_SCHEMA.TABLES 
WHERE 
    TABLE_SCHEMA='cloud_ems' 
    AND TABLE_TYPE='BASE TABLE' 
ORDER BY 
    TABLE_NAME;
```
