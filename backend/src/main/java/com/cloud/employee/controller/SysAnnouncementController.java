package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysAnnouncement;
import com.cloud.employee.service.ISysAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/announcement")
public class SysAnnouncementController {

    @Autowired
    private ISysAnnouncementService announcementService;

    // 分页查询公告列表
    @GetMapping("/list")
    public Result<Page<SysAnnouncement>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type) {
        Page<SysAnnouncement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysAnnouncement> queryWrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            queryWrapper.like(SysAnnouncement::getTitle, title);
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(SysAnnouncement::getType, type);
        }
        queryWrapper.orderByDesc(SysAnnouncement::getCreateTime);
        return Result.success(announcementService.page(page, queryWrapper));
    }

    // 新增公告
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody SysAnnouncement announcement) {
        announcement.setCreateTime(new Date());
        if (announcement.getPublishTime() == null && announcement.getIsPublished() == 1) {
            announcement.setPublishTime(new Date());
        }
        return Result.success(announcementService.save(announcement));
    }

    // 更新公告
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody SysAnnouncement announcement) {
        return Result.success(announcementService.updateById(announcement));
    }

    // 删除公告
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(announcementService.removeById(id));
    }

    // 发布/撤销公告
    @PutMapping("/publish/{id}/{status}")
    public Result<Boolean> publish(@PathVariable Long id, @PathVariable Integer status) {
        SysAnnouncement announcement = new SysAnnouncement();
        announcement.setId(id);
        announcement.setIsPublished(status);
        if (status == 1) {
            announcement.setPublishTime(new Date());
        }
        return Result.success(announcementService.updateById(announcement));
    }

    // 获取单个公告详情
    @GetMapping("/{id}")
    public Result<SysAnnouncement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }
}
