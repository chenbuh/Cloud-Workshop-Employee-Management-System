package com.cloud.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.annotation.Log;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.SysDocCategory;
import com.cloud.employee.entity.SysDocument;
import com.cloud.employee.service.ISysDocCategoryService;
import com.cloud.employee.service.ISysDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doc")
public class DocumentController {

    @Autowired
    private ISysDocCategoryService categoryService;

    @Autowired
    private ISysDocumentService documentService;

    // --- Category Management ---

    @GetMapping("/categories")
    public Result<List<SysDocCategory>> listCategories() {
        return Result.success(categoryService.list(new LambdaQueryWrapper<SysDocCategory>()
                .orderByAsc(SysDocCategory::getOrderNum)));
    }

    @Log(title = "新增文档分类", businessType = 1)
    @PostMapping("/category")
    public Result<Boolean> addCategory(@RequestBody SysDocCategory category) {
        return Result.success(categoryService.save(category));
    }

    @Log(title = "删除文档分类", businessType = 3)
    @DeleteMapping("/category/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        return Result.success(categoryService.removeById(id));
    }

    // --- Document Management ---

    @GetMapping("/list")
    public Result<Page<SysDocument>> listDocuments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Page<SysDocument> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDocument> queryWrapper = new LambdaQueryWrapper<>();
        if (categoryId != null && categoryId != 0) {
            queryWrapper.eq(SysDocument::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(w -> w.like(SysDocument::getTitle, keyword)
                    .or().like(SysDocument::getSummary, keyword));
        }
        queryWrapper.eq(SysDocument::getIsPublished, 1);
        queryWrapper.orderByDesc(SysDocument::getIsPinned);
        queryWrapper.orderByDesc(SysDocument::getCreateTime);
        return Result.success(documentService.page(page, queryWrapper));
    }

    @GetMapping("/{id}")
    public Result<SysDocument> getDocument(@PathVariable Long id) {
        SysDocument doc = documentService.getById(id);
        if (doc != null) {
            documentService.incrementViewCount(id);
        }
        return Result.success(doc);
    }

    @Log(title = "保存知识库文档", businessType = 1)
    @PostMapping("/save")
    public Result<Boolean> saveDocument(@RequestBody SysDocument document) {
        if (document.getId() == null) {
            document.setCreateTime(new Date());
            // In a real app, get current user name from StpUtil/UserService
            document.setCreateBy("管理员");
        }
        document.setUpdateTime(new Date());
        return Result.success(documentService.saveOrUpdate(document));
    }

    @Log(title = "删除文档", businessType = 3)
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDocument(@PathVariable Long id) {
        return Result.success(documentService.removeById(id));
    }
}
