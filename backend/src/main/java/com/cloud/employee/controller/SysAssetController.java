package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysAsset;
import com.cloud.employee.entity.SysAssetClaim;
import com.cloud.employee.service.ISysAssetClaimService;
import com.cloud.employee.service.ISysAssetService;
import com.cloud.employee.service.ISysNotificationService;
import com.cloud.employee.entity.SysNotification;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/asset")
public class SysAssetController {

    @Autowired
    private ISysAssetService assetService;

    @Autowired
    private ISysAssetClaimService claimService;

    @Autowired
    private ISysNotificationService notificationService;

    // --- Asset Management ---
    @GetMapping("/list")
    public Result<List<SysAsset>> listAssets(@RequestParam(required = false) String category) {
        LambdaQueryWrapper<SysAsset> query = new LambdaQueryWrapper<SysAsset>()
                .eq(SysAsset::getStatus, 1);
        if (category != null && !category.isEmpty()) {
            query.eq(SysAsset::getCategory, category);
        }
        return Result.success(assetService.list(query));
    }

    // --- Claim Management ---
    @GetMapping("/claims")
    public Result<List<Map<String, Object>>> listClaims() {
        return Result.success(claimService.getClaimList());
    }

    @PostMapping("/claim")
    public Result<String> createClaim(@RequestBody SysAssetClaim claim) {
        // 1. Validate stock
        SysAsset asset = assetService.getById(claim.getAssetId());
        if (asset == null || asset.getStock() < claim.getCount()) {
            return Result.error("库存不足，无法申领");
        }

        // 2. Setting base fields
        claim.setUserId(StpUtil.getLoginIdAsLong());
        claim.setClaimTime(new Date());
        claim.setStatus(0); // Pending

        claimService.save(claim);
        return Result.success("申请已提交，请等待审批");
    }

    @PutMapping("/claim/approve")
    public Result<String> approveClaim(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";

        if (claimService.approveClaim(id, status, remark)) {
            // Send notification to user
            SysAssetClaim claim = claimService.getById(id);
            SysNotification notify = new SysNotification();
            notify.setUserId(claim.getUserId());
            notify.setTitle(status == 1 ? "物资申领通过" : "物资申领被驳回");
            notify.setContent("您的物资申领申请(" + id + ")处理结果为：" + (status == 1 ? "通过" : "驳回") + "。备注：" + remark);
            notify.setType(status == 1 ? "success" : "warning");
            notify.setIsRead(0);
            notify.setCreateTime(new Date());
            notificationService.save(notify);

            return Result.success("处理成功");
        }
        return Result.error("处理失败，记录可能不存在或已处理");
    }
}
