package com.example.penetration.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.penetration.entity.User;
import com.example.penetration.mapper.UserMapper;
import com.example.penetration.request.UserCreateRequest;
import com.example.penetration.request.UserLoginRequest;
import com.example.penetration.request.UserUpdataRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
    private final UserMapper userMapper;

    public Boolean usernameExist(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper) != null;
    }

    public void register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("user"); // 默认注册为普通用户
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    public User login(UserLoginRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        queryWrapper.eq("password", request.getPassword());
        User user = userMapper.selectOne(queryWrapper);
        //return userMapper.selectOne(queryWrapper);

        // 检查角色是否匹配
        if (user != null && request.getRole() != null && !request.getRole().equals(user.getRole())) {
            return null; // 角色不匹配
        }

        return user;
    }


    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 获取所有用户列表（管理员权限）
     */
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    /**
     * 删除用户（管理员权限）
     */
    public boolean deleteUser(Integer id) {

        return userMapper.deleteById(id) > 0;
    }

    /**
     * 更新用户角色（管理员权限）
     */
    public boolean updateUserRole(Integer id, String role) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }
        user.setRole(role);
        return userMapper.updateById(user) > 0;
    }

    /**
     * 管理员创建用户
     */
    public User createUser(UserCreateRequest request) {
        if (usernameExist(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    /**
     * 管理员更新用户信息
     */
    public User updateUser(Integer id, UserUpdataRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户名（需检查是否重复）
        if (!user.getUsername().equals(request.getUsername())) {
            if (usernameExist(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }

        // 更新密码（如果有提供）
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword()); // 明文更新密码
        }

        // 更新角色（如果有提供）
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            user.setRole(request.getRole());
        }

        userMapper.updateById(user);
        return user;
    }


    /**
     * 获取用户详情
     */
    public User getUserById(Integer id) {
        return userMapper.selectById(id);
    }

    public User updateProfile(String currentUsername, String newUsername, String newPassword, String currentPassword) {
        // 查询当前用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", currentUsername);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证当前密码（明文比较）
        if (!currentPassword.equals(user.getPassword())) {
            throw new RuntimeException("当前密码不正确");
        }

        // 检查新用户名是否已存在
        if (!currentUsername.equals(newUsername)) {
            QueryWrapper<User> usernameQuery = new QueryWrapper<>();
            usernameQuery.eq("username", newUsername);
            if (userMapper.selectCount(usernameQuery) > 0) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(newUsername);
        }

        // 更新密码（明文存储）
        if (StringUtils.hasText(newPassword)) {
            user.setPassword(newPassword);
        }

        // 更新用户信息
        userMapper.updateById(user);
        return user;
    }


}