package com.cloud.employee.aspect;

import cn.dev33.satoken.stp.StpUtil;

import com.alibaba.fastjson2.JSON;
import com.cloud.employee.annotation.Log;
import com.cloud.employee.entity.SysAuditLog;
import com.cloud.employee.entity.SysUser;
import com.cloud.employee.service.ISysAuditLogService;
import com.cloud.employee.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private ISysAuditLogService auditLogService;

    @Autowired
    private ISysUserService userService;

    /**
     * 处理完请求后执行
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes == null)
                return;
            HttpServletRequest request = attributes.getRequest();

            SysAuditLog operLog = new SysAuditLog();
            operLog.setStatus(1); // 1-Success

            // IP & URL
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            operLog.setOperIp(ip);
            operLog.setOperUrl(request.getRequestURI());
            operLog.setRequestMethod(request.getMethod());

            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                operLog.setOperatorId(userId);
                SysUser user = userService.getById(userId);
                if (user != null) {
                    operLog.setOperatorName(user.getNickName());
                }
            }

            if (e != null) {
                operLog.setStatus(0); // 0-Error
                operLog.setErrorMsg(e.getMessage());
            }

            // Annotation info
            operLog.setTitle(controllerLog.title());
            operLog.setBusinessType(controllerLog.businessType());

            // Method
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");

            // Results
            if (jsonResult != null) {
                operLog.setJsonResult(JSON.toJSONString(jsonResult));
            }

            operLog.setOperTime(new Date());

            // Save log
            auditLogService.save(operLog);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
