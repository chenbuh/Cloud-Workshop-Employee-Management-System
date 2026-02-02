package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysDocumentMapper extends BaseMapper<SysDocument> {

    @Update("UPDATE sys_document SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);
}
