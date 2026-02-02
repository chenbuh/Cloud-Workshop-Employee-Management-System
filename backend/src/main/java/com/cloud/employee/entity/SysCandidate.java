package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_candidate")
public class SysCandidate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long candidateId;
    private String fullName;
    private String phone;
    private String email;
    private Long postId;
    private String resumeUrl;
    private String source;
    private Integer status; // 1初筛 2面试中 3待入职 4已拒绝 5已入职
    private Date createTime;
    private Date updateTime;
}
