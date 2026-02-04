package com.cloud.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.employee.entity.SysAssetClaim;

import java.util.List;
import java.util.Map;

public interface ISysAssetClaimService extends IService<SysAssetClaim> {
    List<Map<String, Object>> getClaimList();

    boolean approveClaim(Long id, Integer status, String remark);
}
