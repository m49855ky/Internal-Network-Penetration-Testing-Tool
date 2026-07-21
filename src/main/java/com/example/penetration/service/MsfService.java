package com.example.penetration.service;

import com.example.penetration.entity.MsfEntity;
import com.example.penetration.entity.User;
import com.example.penetration.mapper.MsfMapper;
import com.example.penetration.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MsfService {
    private final MsfMapper msfMapper;
    private final UserMapper userMapper;


    // 获取渗透测试记录详情(包含用户信息)
    public MsfEntity getAttackDetail(Integer id) {
        MsfEntity attack = msfMapper.selectById(id);
        if (attack != null && attack.getUserId() != null) {
            User user = userMapper.selectById(attack.getUserId());
        }
        return attack;
    }

}