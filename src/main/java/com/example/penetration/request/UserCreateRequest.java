package com.example.penetration.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String role = "user"; // 默认普通用户
}
