package com.example.penetration.service;

import com.example.penetration.entity.OSScanEntity;
import com.example.penetration.entity.User;
import com.example.penetration.mapper.OSScanMapper;
import com.example.penetration.mapper.UserMapper;
import com.example.penetration.utils.NmapUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OSScanService {
    private final OSScanMapper osScanMapper;
    private final NmapUtils nmapUtils;
    private final UserMapper userMapper;

    public OSScanEntity scan(String ip, Integer userId) {
        OSScanEntity osScanEntity = nmapUtils.readOS(ip);
        osScanEntity.setUserId(userId);
        osScanMapper.insert(osScanEntity);
        return osScanEntity;
    }


    // 获取OS扫描记录详情(包含用户信息)
    public OSScanEntity getOSScanDetail(Integer id) {
        OSScanEntity osScan = osScanMapper.selectById(id);
        if (osScan != null && osScan.getUserId() != null) {
            User user = userMapper.selectById(osScan.getUserId());
        }
        return osScan;
    }
}