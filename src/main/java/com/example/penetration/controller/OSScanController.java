package com.example.penetration.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.penetration.annotation.RequiresPermission;
import com.example.penetration.common.Result;
import com.example.penetration.entity.OSScanEntity;
import com.example.penetration.mapper.OSScanMapper;
import com.example.penetration.service.OSScanService;
import com.example.penetration.utils.NmapUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/os")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OSScanController {
    private final OSScanService osScanService;
    private final OSScanMapper osScanMapper;
    private final NmapUtils nmapUtils;

    @GetMapping("/scan")
    public Result scan(@RequestParam String ip, @RequestParam Integer userId) {
        OSScanEntity osScanEntity = osScanService.scan(ip, userId);
        return Result.success(osScanEntity);
    }

    // 新增：批量扫描所有在线IP的操作系统
    @GetMapping("/batch-scan")
    public Result batchScan(@RequestParam Integer userId) {
        List<OSScanEntity> results = nmapUtils.batchScanOS().join();
        // 保存扫描结果到数据库
        results.forEach(entity -> {
            entity.setUserId(userId);
            osScanMapper.insert(entity);
        });
        return Result.success(results);
    }


    // 获取所有OS扫描记录(管理员)
    @GetMapping("/all")
    @RequiresPermission(role = "admin")
    public Result getAllOSScans() {
        List<OSScanEntity> list = osScanMapper.selectList(null);
        return Result.success(list);
    }

    // 按用户ID获取OS扫描记录(管理员)
    @GetMapping("/user/{userId}")
    @RequiresPermission(role = "admin")
    public Result getOSScansByUserId(@PathVariable Integer userId) {
        QueryWrapper<OSScanEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<OSScanEntity> list = osScanMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    // 添加OS扫描记录(管理员)
    @PostMapping("/add")
    @RequiresPermission(role = "admin")
    public Result addOSScan(@RequestBody OSScanEntity osScanEntity) {
        int result = osScanMapper.insert(osScanEntity);
        return result > 0 ? Result.success("添加成功") : Result.failure("添加失败");
    }

    // 更新OS扫描记录(管理员)
    @PutMapping("/update")
    @RequiresPermission(role = "admin")
    public Result updateOSScan(@RequestBody OSScanEntity osScanEntity) {
        int result = osScanMapper.updateById(osScanEntity);
        return result > 0 ? Result.success("更新成功") : Result.failure("更新失败");
    }

    // 删除OS扫描记录(管理员)
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(role = "admin")
    public Result deleteOSScan(@PathVariable Integer id) {
        int result = osScanMapper.deleteById(id);
        return result > 0 ? Result.success("删除成功") : Result.failure("删除失败");
    }

    // 条件查询OS扫描记录(管理员)
    @GetMapping("/search")
    @RequiresPermission(role = "admin")
    public Result searchOSScans(
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) Integer userId) {
        QueryWrapper<OSScanEntity> queryWrapper = new QueryWrapper<>();
        if (ip != null) {
            queryWrapper.eq("ip", ip);
        }
        if (os != null) {
            queryWrapper.like("os", os);
        }
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        List<OSScanEntity> list = osScanMapper.selectList(queryWrapper);
        return Result.success(list);
    }
}