package com.example.penetration.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.penetration.annotation.RequiresPermission;
import com.example.penetration.common.Result;
import com.example.penetration.entity.PortScanEntity;
import com.example.penetration.mapper.PortScanMapper;
import com.example.penetration.service.PortScanService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/port")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PortScanController {
    public final PortScanService portScanService;
    private final PortScanMapper portScanMapper;

    @GetMapping("/scan")
    public Result scan(@RequestParam String ip, @RequestParam Integer userId) {
        List<PortScanEntity> list = portScanService.scan(ip,userId);
        return Result.success(list);
    }


    // 获取所有端口扫描记录(管理员)
    @GetMapping("/all")
    @RequiresPermission(role = "admin")
    public Result getAllPortScans() {
        List<PortScanEntity> list = portScanMapper.selectList(null);
        return Result.success(list);
    }

    // 按用户ID获取端口扫描记录(管理员)
    @GetMapping("/user/{userId}")
    @RequiresPermission(role = "admin")
    public Result getPortScansByUserId(@PathVariable Integer userId) {
        QueryWrapper<PortScanEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<PortScanEntity> list = portScanMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    // 添加端口扫描记录(管理员)
    @PostMapping("/add")
    @RequiresPermission(role = "admin")
    public Result addPortScan(@RequestBody PortScanEntity portScanEntity) {
        int result = portScanMapper.insert(portScanEntity);
        return result > 0 ? Result.success("添加成功") : Result.failure("添加失败");
    }

    // 更新端口扫描记录(管理员)
    @PutMapping("/update")
    @RequiresPermission(role = "admin")
    public Result updatePortScan(@RequestBody PortScanEntity portScanEntity) {
        int result = portScanMapper.updateById(portScanEntity);
        return result > 0 ? Result.success("更新成功") : Result.failure("更新失败");
    }

    // 删除端口扫描记录(管理员)
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(role = "admin")
    public Result deletePortScan(@PathVariable Integer id) {
        int result = portScanMapper.deleteById(id);
        return result > 0 ? Result.success("删除成功") : Result.failure("删除失败");
    }

    // 条件查询端口扫描记录(管理员)
    @GetMapping("/search")
    @RequiresPermission(role = "admin")
    public Result searchPortScans(
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer port,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) Integer userId) {
        QueryWrapper<PortScanEntity> queryWrapper = new QueryWrapper<>();
        if (ip != null) {
            queryWrapper.eq("ip", ip);
        }
        if (port != null) {
            queryWrapper.eq("port", port);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (protocol != null) {
            queryWrapper.eq("protocol", protocol);
        }
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        List<PortScanEntity> list = portScanMapper.selectList(queryWrapper);
        return Result.success(list);
    }
}
