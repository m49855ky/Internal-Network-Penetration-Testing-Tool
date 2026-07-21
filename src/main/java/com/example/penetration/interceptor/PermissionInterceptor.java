package com.example.penetration.interceptor;

import com.example.penetration.annotation.RequiresPermission;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 检查方法上的注解
        RequiresPermission methodAnnotation = method.getAnnotation(RequiresPermission.class);
        if (methodAnnotation != null) {
            return checkPermission(request, methodAnnotation);
        }

        // 检查类上的注解
        RequiresPermission classAnnotation = method.getDeclaringClass().getAnnotation(RequiresPermission.class);
        if (classAnnotation != null) {
            return checkPermission(request, classAnnotation);
        }

        return true;
    }

    private boolean checkPermission(HttpServletRequest request, RequiresPermission permission) {
        HttpSession session = request.getSession(false);

        // 检查是否登录
        if (session == null || session.getAttribute("user") == null) {
            throw new RuntimeException("未登录，请先登录");
        }

        // 获取用户角色
        String role = (String) session.getAttribute("role");

        // 验证角色
        if (permission.role().equals(role)) {
            return true;
        }

        // 没有权限
        throw new RuntimeException("没有权限访问");
    }
}