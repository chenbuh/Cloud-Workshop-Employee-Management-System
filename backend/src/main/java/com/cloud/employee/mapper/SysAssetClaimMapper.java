package com.cloud.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.employee.entity.SysAssetClaim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysAssetClaimMapper extends BaseMapper<SysAssetClaim> {

    @Select("SELECT c.*, a.name as assetName, a.unit, u.nick_name as userName " +
            "FROM sys_asset_claim c " +
            "LEFT JOIN sys_asset a ON c.asset_id = a.id " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "ORDER BY c.claim_time DESC")
    List<Map<String, Object>> selectClaimList();
}
