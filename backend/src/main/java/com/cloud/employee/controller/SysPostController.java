package com.cloud.employee.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysPost;
import com.cloud.employee.service.ISysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/system/post")
public class SysPostController {

    @Autowired
    private ISysPostService postService;

    @GetMapping("/list")
    @SaCheckPermission("system:post:list")
    public Result<Page<SysPost>> list(Page<SysPost> page, String postName, String postCode, String status) {
        LambdaQueryWrapper<SysPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(postName != null, SysPost::getPostName, postName);
        queryWrapper.like(postCode != null, SysPost::getPostCode, postCode);
        queryWrapper.eq(status != null, SysPost::getStatus, status);
        queryWrapper.orderByAsc(SysPost::getPostSort);
        return Result.success(postService.page(page, queryWrapper));
    }

    @GetMapping("/all")
    public Result<List<SysPost>> all() {
        return Result.success(postService
                .list(new LambdaQueryWrapper<SysPost>().eq(SysPost::getStatus, "0").orderByAsc(SysPost::getPostSort)));
    }

    @GetMapping("/{postId}")
    @SaCheckPermission("system:post:query")
    public Result<SysPost> getInfo(@PathVariable Long postId) {
        return Result.success(postService.getById(postId));
    }

    @PostMapping
    @SaCheckPermission("system:post:add")
    public Result<Boolean> add(@RequestBody SysPost post) {
        return Result.success(postService.save(post));
    }

    @PutMapping
    @SaCheckPermission("system:post:edit")
    public Result<Boolean> edit(@RequestBody SysPost post) {
        return Result.success(postService.updateById(post));
    }

    @DeleteMapping("/{postIds}")
    @SaCheckPermission("system:post:remove")
    public Result<Boolean> remove(@PathVariable List<Long> postIds) {
        return Result.success(postService.removeByIds(postIds));
    }
}
