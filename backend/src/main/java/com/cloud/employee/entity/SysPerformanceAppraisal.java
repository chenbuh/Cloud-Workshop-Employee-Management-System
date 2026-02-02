package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sys_performance_appraisal")
public class SysPerformanceAppraisal implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cycleId;
    private Long empId;
    private BigDecimal score;
    private String rating;
    private Integer potentialScore;
    private String comment;
    private Long reviewerId;
    private Integer status; // 1待评 2已评价
    private Date createTime;
}
