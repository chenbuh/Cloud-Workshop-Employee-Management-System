package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("rec_interview")
public class RecInterview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long candidateId;
    private Long interviewerId;
    private Date interviewTime;
    private Integer type; // 1初试 2复试 3终试
    private String linkUrl;
    private Integer score;
    private String comment;
    private Integer result; // 0待反馈 1通过 2不通过
    private Integer status; // 0待面试 1已完成 2已取消
    private Date createTime;
}
