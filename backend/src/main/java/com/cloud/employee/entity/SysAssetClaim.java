package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_asset_claim")
public class SysAssetClaim {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Long userId;
    private Integer count;
    private String reason;
    private Integer status; // 0待审核 1已发放 2已驳回
    private Date claimTime;
    private Date processTime;
    private String processRemark;
}
