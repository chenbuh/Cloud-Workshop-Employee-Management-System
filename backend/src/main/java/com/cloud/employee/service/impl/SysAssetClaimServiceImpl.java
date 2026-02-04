package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysAsset;
import com.cloud.employee.entity.SysAssetClaim;
import com.cloud.employee.mapper.SysAssetClaimMapper;
import com.cloud.employee.service.ISysAssetClaimService;
import com.cloud.employee.service.ISysAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SysAssetClaimServiceImpl extends ServiceImpl<SysAssetClaimMapper, SysAssetClaim>
        implements ISysAssetClaimService {

    @Autowired
    private ISysAssetService assetService;

    @Override
    public List<Map<String, Object>> getClaimList() {
        return baseMapper.selectClaimList();
    }

    @Override
    @Transactional
    public boolean approveClaim(Long id, Integer status, String remark) {
        SysAssetClaim claim = getById(id);
        if (claim == null || claim.getStatus() != 0)
            return false;

        claim.setStatus(status);
        claim.setProcessRemark(remark);
        claim.setProcessTime(new Date());
        updateById(claim);

        // If approved (status = 1), reduce stock
        if (status == 1) {
            SysAsset asset = assetService.getById(claim.getAssetId());
            if (asset != null) {
                asset.setStock(asset.getStock() - claim.getCount());
                assetService.updateById(asset);
            }
        }
        return true;
    }
}
