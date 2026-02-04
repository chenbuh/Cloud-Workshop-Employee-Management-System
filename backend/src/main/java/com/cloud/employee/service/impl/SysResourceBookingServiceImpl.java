package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysResource;
import com.cloud.employee.entity.SysResourceBooking;
import com.cloud.employee.mapper.SysResourceBookingMapper;
import com.cloud.employee.service.ISysResourceBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysResourceBookingServiceImpl extends ServiceImpl<SysResourceBookingMapper, SysResourceBooking>
        implements ISysResourceBookingService {

    @Override
    public List<Map<String, Object>> getBookingList() {
        return baseMapper.selectBookingList();
    }

    @Override
    public boolean checkConflict(Long resourceId, Date start, Date end) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<SysResourceBooking>()
                .eq(SysResourceBooking::getResourceId, resourceId)
                .eq(SysResourceBooking::getStatus, 1)
                .and(w -> w.lt(SysResourceBooking::getStartTime, end)
                        .gt(SysResourceBooking::getEndTime, start)));
        return count > 0;
    }

    @Autowired
    private com.cloud.employee.mapper.SysResourceMapper resourceMapper;

    @Autowired
    private com.cloud.employee.mapper.SysAssetClaimMapper assetClaimMapper;

    @Override
    public Map<String, Object> getUsageAnalytics() {
        Map<String, Object> result = new HashMap<>();

        // 1. Meeting room bookings by day (last 7 days)
        List<Map<String, Object>> bookingTrend = baseMapper.selectMaps(new QueryWrapper<SysResourceBooking>()
                .select("DATE(start_time) as date", "COUNT(*) as count")
                .ge("start_time", new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000))
                .groupBy("DATE(start_time)")
                .orderByAsc("DATE(start_time)"));
        result.put("bookingTrend", bookingTrend);

        // 2. Resource popularity (Top booked rooms)
        List<Map<String, Object>> resourcePop = baseMapper.selectMaps(new QueryWrapper<SysResourceBooking>()
                .select("resource_id", "COUNT(*) as count")
                .groupBy("resource_id")
                .orderByDesc("count")
                .last("LIMIT 5"));

        for (Map<String, Object> item : resourcePop) {
            Object resId = item.get("resource_id");
            if (resId != null) {
                SysResource r = resourceMapper.selectById(Long.valueOf(resId.toString()));
                item.put("name", r != null ? r.getName() : "未知");
            }
        }
        result.put("resourcePopularity", resourcePop);

        // 3. Asset Claim Stats
        List<Map<String, Object>> assetStats = assetClaimMapper
                .selectMaps(new QueryWrapper<com.cloud.employee.entity.SysAssetClaim>()
                        .select("status", "COUNT(*) as count")
                        .groupBy("status"));
        result.put("assetClaimStatus", assetStats);

        return result;
    }
}
