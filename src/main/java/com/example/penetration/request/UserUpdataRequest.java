package com.example.penetration.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserUpdataRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    private String role;
}
