package com.example.penetration.service;

import com.example.penetration.entity.PortScanEntity;
import com.example.penetration.entity.User;
import com.example.penetration.mapper.PortScanMapper;
import com.example.penetration.mapper.UserMapper;
import com.example.penetration.utils.NmapUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PortScanService {
    private final PortScanMapper portScanMapper;
    private final NmapUtils nmapUtils;
    private final UserMapper userMapper;

    public List<PortScanEntity> scan(String ip, Integer userId) {
        List<PortScanEntity> list = nmapUtils.readPort(ip);
        for (PortScanEntity portScanEntity : list) {
            portScanEntity.setUserId(userId);
            portScanMapper.insert(portScanEntity);
        }
        return list;
    }


    // 获取端口扫描记录详情(包含用户信息)
    public PortScanEntity getPortScanDetail(Integer id) {
        PortScanEntity portScan = portScanMapper.selectById(id);
        if (portScan != null && portScan.getUserId() != null) {
            User user = userMapper.selectById(portScan.getUserId());
        }
        return portScan;
    }
}