package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("rec_candidate")
public class RecCandidate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String education;
    private Integer experienceYears;
    private String resumeUrl;
    private Long applyJobId;
    private Integer status; // 1新简历 2初筛通过 3面试中 4已发Offer 5已入职 9淘汰
    private Date createTime;
    private Date updateTime;
}
