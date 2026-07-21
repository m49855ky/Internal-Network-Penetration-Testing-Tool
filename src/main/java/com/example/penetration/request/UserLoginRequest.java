package com.example.penetration.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String username;
    private String password;
    private String role;
}