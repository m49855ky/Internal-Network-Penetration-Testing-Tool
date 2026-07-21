package com.example.penetration.annotation;

import java.lang.annotation.*;

//创建权限注解
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
        String[] value() default {};
        String role() default "admin";
    }
