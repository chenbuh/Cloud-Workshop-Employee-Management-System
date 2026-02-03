package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("rec_job")
public class RecJob {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobTitle;
    private Long deptId;
    private Integer headCount;
    private String jobDesc;
    private String requirements;
    private String salaryRange;
    private String location;
    private Integer status;
    private String createBy;
    private Date createTime;
}
