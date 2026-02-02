package com.cloud.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_document")
public class SysDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long categoryId;
    private String docType; // article, file
    private String fileUrl;
    private Integer viewCount;
    private Integer isPinned;
    private String tags;
    private Integer isPublished;
    private String createBy;
    private Date createTime;
    private Date updateTime;
}
