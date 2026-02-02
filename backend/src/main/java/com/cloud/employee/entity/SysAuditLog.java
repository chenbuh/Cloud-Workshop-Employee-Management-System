package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_audit_log")
public class SysAuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Integer businessType; // 0其它 1新增 2修改 3删除
    private String method;
    private String requestMethod;
    private Long operatorId;
    private String operatorName;
    private String operUrl;
    private String operIp;
    private String operParam;
    private String jsonResult;
    private Integer status; // 1正常 0异常
    private String errorMsg;
    private Date operTime;
}
