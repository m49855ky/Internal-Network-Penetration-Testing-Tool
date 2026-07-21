package com.example.penetration.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.penetration.annotation.RequiresPermission;
import com.example.penetration.common.Result;
import com.example.penetration.entity.IPScanEntity;
import com.example.penetration.mapper.IPScanMapper;
import com.example.penetration.service.IPScanService;
import com.example.penetration.utils.NmapUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/ip")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IPScanController {
    private final IPScanService ipScanService;
    private  final IPScanMapper ipScanMapper;
    private final NmapUtils nmapUtils;

    @GetMapping("/scan")
    public Result iPScan(@RequestParam String ipRange,@RequestParam Integer userId) {
        List<IPScanEntity> list = ipScanService.readMap(ipRange, userId);
        return Result.success(list);
    }

    // 获取所有在线IP
    @GetMapping("/online")
    public Result getOnlineIps() {
        Set<String> onlineIps = nmapUtils.getOnlineIps();
        return Result.success(onlineIps);
    }

    //获取所有IP扫描记录（管理员）
    @GetMapping("/all")
    @RequiresPermission(role = "admin")
    public Result getAllIPScans() {
        List<IPScanEntity> list = ipScanMapper.selectList(null);
        return Result.success(list);
    }

    // 按用户ID获取IP扫描记录(管理员)
    @GetMapping("/user/{userId}")
    @RequiresPermission(role = "admin")
    public Result getIPScansByUserId(@PathVariable Integer userId) {
        QueryWrapper<IPScanEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<IPScanEntity> list = ipScanMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    //添加ip扫描记录（管理员）
    @PostMapping("/add")
    @RequiresPermission(role = "admin")
    public Result addIPScan(@RequestBody IPScanEntity ipScanEntity) {
        int result = ipScanMapper.insert(ipScanEntity);
        return result > 0 ? Result.success("添加成功") : Result.failure("添加失败");
    }

    //更新ip扫描记录（管理员）
    @PutMapping("/update")
    @RequiresPermission(role = "admin")
    public Result updateIPScan(@RequestBody IPScanEntity ipScanEntity) {
        int result = ipScanMapper.updateById(ipScanEntity);
        return result > 0 ? Result.success("更新成功") : Result.failure("更新失败");
    }

    //删除IP扫描记录（管理员）
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(role = "admin")
    public Result deleteIPScan(@PathVariable Integer id) {
        int result = ipScanMapper.deleteById(id);
        return result > 0 ? Result.success("删除成功") : Result.failure("删除失败");
    }

    // 条件查询IP扫描记录(管理员)
    @GetMapping("/search")
    @RequiresPermission(role = "admin")
    public Result searchIPScans(
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer userId) {
        QueryWrapper<IPScanEntity> queryWrapper = new QueryWrapper<>();
        if (ip != null) {
            queryWrapper.eq("ip", ip);
        }
        if (status != null) {
            queryWrapper.eq("status", status.toLowerCase());
        }
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        List<IPScanEntity> list = ipScanMapper.selectList(queryWrapper);
        return Result.success(list);
    }
}
