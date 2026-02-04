package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysResourceBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysResourceBookingMapper extends BaseMapper<SysResourceBooking> {

    @Select("SELECT b.*, r.name as resourceName, u.nick_name as userName " +
            "FROM sys_resource_booking b " +
            "LEFT JOIN sys_resource r ON b.resource_id = r.id " +
            "LEFT JOIN sys_user u ON b.user_id = u.id " +
            "ORDER BY b.start_time DESC")
    List<Map<String, Object>> selectBookingList();
}
