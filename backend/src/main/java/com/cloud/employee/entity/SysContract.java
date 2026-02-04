package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_contract")
public class SysContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long employeeId;
    private String title;
    private String type;
    private Integer status; // 0草稿 1有效 2过期 3终止
    private String content;
    private Date startDate;
    private Date endDate;
    private Date signDate;
    private Date createTime;
}
