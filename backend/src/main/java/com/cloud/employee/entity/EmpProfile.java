package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("emp_profile")
public class EmpProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long deptId; // 部门ID
    private String empCode;
    private String fullName;
    private String idCard;
    private Date birthday;
    private Date entryDate;
    private Date probationEndDate;
    private String jobTitle;
    private Long postId;
    private Long levelId;
    private String resumeUrl;
    private String workLocation;
    private String education;
    private String major;
    private String emergencyContact;
    private String emergencyPhone;
    private Integer status; // 1在职 2试用 3离职 4退休
    private Date quitDate; // 离职日期
    private String quitReason; // 离职原因
    private Date createTime;
    private Date updateTime;
}
