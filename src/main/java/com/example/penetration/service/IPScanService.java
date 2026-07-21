package com.example.penetration.service;

import com.example.penetration.entity.IPScanEntity;
import com.example.penetration.entity.User;
import com.example.penetration.mapper.IPScanMapper;
import com.example.penetration.mapper.UserMapper;
import com.example.penetration.utils.NmapUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IPScanService {
    private final NmapUtils nmapUtils;
    private final IPScanMapper ipScanMapper;
    private final UserMapper userMapper;
    public List<IPScanEntity> readMap(String ipRange, Integer userId) {
        List<IPScanEntity> list = nmapUtils.readNmap(ipRange);
        for (IPScanEntity ipScanEntity : list) {
            ipScanEntity.setUserId(userId);
            System.out.println(ipScanEntity);
            ipScanMapper.insert(ipScanEntity);
        }
        return list;
    }


    // 获取IP扫描记录详情(包含用户信息)
    public IPScanEntity getIPScanDetail(Integer id) {
        IPScanEntity ipScan = ipScanMapper.selectById(id);
        if (ipScan != null && ipScan.getUserId() != null) {
            User user = userMapper.selectById(ipScan.getUserId());
        }
        return ipScan;
    }
}