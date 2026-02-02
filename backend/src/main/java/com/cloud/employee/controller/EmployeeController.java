package com.cloud.employee.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.employee.annotation.Log;
import com.cloud.employee.common.result.Result;
import com.cloud.employee.entity.EmpProfile;
import com.cloud.employee.service.IEmpProfileService;
import com.cloud.employee.vo.EmpProfileVO;
import com.cloud.employee.vo.EmployeeExportVO;
import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    @Autowired
    private IEmpProfileService empProfileService;

    @GetMapping("/export")
    public void export(HttpServletResponse response,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId) throws IOException {
        List<EmployeeExportVO> list = empProfileService.listAllForExport(keyword, deptId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("员工信息表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), EmployeeExportVO.class).sheet("员工列表").doWrite(list);
    }

    // 分页列表
    @GetMapping("/list")
    public Result<Page<EmpProfileVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId) {
        Page<EmpProfileVO> page = new Page<>(pageNum, pageSize);
        return Result.success(empProfileService.getEmployeePage(page, keyword, deptId));
    }

    // 新增员工 (入职)
    @Log(title = "员工入职", businessType = 1)
    @PostMapping("/onboard")
    public Result<Boolean> onboard(@RequestBody EmpProfile empProfile) {
        empProfile.setCreateTime(new Date());
        empProfile.setStatus(2);
        if (!org.springframework.util.StringUtils.hasText(empProfile.getEmpCode())) {
            empProfile.setEmpCode("E" + System.currentTimeMillis());
        }
        return Result.success(empProfileService.save(empProfile));
    }

    // 获取详情
    @GetMapping("/{id}")
    public Result<EmpProfile> getInfo(@PathVariable Long id) {
        return Result.success(empProfileService.getById(id));
    }

    // 修改员工
    @PutMapping
    public Result<Boolean> update(@RequestBody EmpProfile empProfile) {
        empProfile.setUpdateTime(new Date());
        return Result.success(empProfileService.updateById(empProfile));
    }

    // 删除员工
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        // 软删除或硬删除，这里先简单实现
        return Result.success(empProfileService.removeById(id));
    }

    // 离职手续办理
    @Log(title = "员工离职", businessType = 2)
    @PostMapping("/resign")
    public Result<Boolean> resign(@RequestBody ResignBody body) {
        EmpProfile profile = empProfileService.getById(body.getId());
        if (profile == null)
            return Result.error("员工不存在");
        profile.setStatus(3);
        profile.setQuitDate(body.getQuitDate());
        profile.setQuitReason(body.getQuitReason());
        profile.setUpdateTime(new Date());
        return Result.success(empProfileService.updateById(profile));
    }

    @lombok.Data
    public static class ResignBody {
        private Long id;
        private java.util.Date quitDate;
        private String quitReason;
    }
}
