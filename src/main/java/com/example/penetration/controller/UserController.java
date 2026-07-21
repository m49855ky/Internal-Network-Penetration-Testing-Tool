package com.example.penetration.controller;

import com.example.penetration.annotation.RequiresPermission;
import com.example.penetration.common.Result;
import com.example.penetration.entity.User;
import com.example.penetration.request.*;
import com.example.penetration.service.UserService;
import com.example.penetration.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    private final UserService userService;

    // 注册接口不需要权限控制
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return Result.failure("密码输入不一致，请重新注册!");
        }
        Boolean flag = userService.usernameExist(request.getUsername());
        if (!flag) {
            userService.register(request.getUsername(), request.getPassword());
            return Result.success();
        }
        return Result.failure("用户名已存在");
    }

    // 登录接口不需要权限控制
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginRequest request, HttpServletRequest httpRequest) {
        User user = userService.login(request);
        if(user == null){
            return Result.failure("用户名或密码错误，登录失败");
        }

        // 登录成功后设置Session
        SessionUtil.setLoginUser(httpRequest, user.getUsername(), user.getRole());

        return Result.success(user);
    }

    // 登出接口
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        SessionUtil.logout(request);
        return Result.success("登出成功");
    }

    // 获取当前用户信息 - 需要登录但不需要特定角色
    @GetMapping("/info")
    public Result getCurrentUser(HttpServletRequest request) {
        String username = SessionUtil.getCurrentUser(request);
        if (username == null) {
            return Result.failure("未登录");
        }
        User user = userService.findByUsername(username);
        return Result.success(user);
    }

    // 管理员专用接口 - 获取所有用户列表
    @GetMapping("/list")
    @RequiresPermission(role = "admin")
    public Result getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    // 管理员专用接口 - 创建用户
    @PostMapping("/create")
    @RequiresPermission(role = "admin")
    public Result createUser(@RequestBody @Valid UserCreateRequest request) {
        try {
            User user = userService.createUser(request);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.failure(e.getMessage());
        }
    }

    // 管理员专用接口 - 更新用户信息
    @PutMapping("/update/{id}")
    @RequiresPermission(role = "admin")
    public Result updateUser(@PathVariable Integer id, @RequestBody @Valid UserUpdataRequest request) {
        try {
            User updatedUser = userService.updateUser(id, request);
            return Result.success(updatedUser);
        } catch (RuntimeException e) {
            return Result.failure(e.getMessage());
        }
    }

    // 管理员专用接口 - 删除用户
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(role = "admin")
    public Result deleteUser(@PathVariable Integer id) {
        boolean result = userService.deleteUser(id);
        return result ? Result.success() : Result.failure("删除用户失败");
    }

    // 用户修改自己的用户名和密码
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody @Valid UserUpdateProfileRequest request, HttpServletRequest httpRequest) {
        String currentUsername = SessionUtil.getCurrentUser(httpRequest);
        if (currentUsername == null) {
            return Result.failure("未登录");
        }

        try {
            User updatedUser = userService.updateProfile(
                    currentUsername,
                    request.getUsername(),
                    request.getNewPassword(),
                    request.getCurrentPassword()
            );

            // 更新session中的用户名
            SessionUtil.setLoginUser(httpRequest, updatedUser.getUsername(), updatedUser.getRole());

            return Result.success(updatedUser);
        } catch (RuntimeException e) {
            return Result.failure(e.getMessage());
        }
    }

}