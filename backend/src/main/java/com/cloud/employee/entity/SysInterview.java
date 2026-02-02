package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_interview")
public class SysInterview implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long interviewId;
    private Long candidateId;
    private Long interviewerId;
    private Date interviewTime;
    private Integer round;
    private Integer score;
    private String comment;
    private Integer status; // 1待面试 2通过 3拒绝
    private Date createTime;
}
