package com.example.penetration.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.penetration.annotation.RequiresPermission;
import com.example.penetration.common.Result;
import com.example.penetration.entity.MsfEntity;
import com.example.penetration.mapper.MsfMapper;
import com.example.penetration.utils.CommandExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attack")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AttackController {
    private final CommandExecutor commandExecutor;
    private final MsfMapper msfMapper;

    @GetMapping("/done")
    public Result done(@RequestParam String ip,
                       @RequestParam String vulnerabilityName,
                       @RequestParam String vulnerabilityModule,
                       @RequestParam Integer userId) {
        try {
            // 调用攻击方法并获取输出结果
            String output = commandExecutor.attackLevel(ip, vulnerabilityName, vulnerabilityModule, userId);

            // 判断是否成功（根据输出内容）
            if (output.contains("NT AUTHORITY\\SYSTEM")) {
                return Result.success(output); // 渗透成功
            } else {
                return Result.failure("渗透失败，请检查日志"); // 渗透失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("渗透测试失败");
        }
    }



    // 获取所有渗透测试记录(管理员)
    @GetMapping("/all")
    @RequiresPermission(role = "admin")
    public Result getAllAttacks() {
        List<MsfEntity> list = msfMapper.selectList(null);
        return Result.success(list);
    }

    // 按用户ID获取渗透测试记录(管理员)
    @GetMapping("/user/{userId}")
    @RequiresPermission(role = "admin")
    public Result getAttacksByUserId(@PathVariable Integer userId) {
        QueryWrapper<MsfEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<MsfEntity> list = msfMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    // 添加渗透测试记录(管理员)
    @PostMapping("/add")
    @RequiresPermission(role = "admin")
    public Result addAttack(@RequestBody MsfEntity msfEntity) {
        int result = msfMapper.insert(msfEntity);
        return result > 0 ? Result.success("添加成功") : Result.failure("添加失败");
    }

    // 更新渗透测试记录(管理员)
    @PutMapping("/update")
    @RequiresPermission(role = "admin")
    public Result updateAttack(@RequestBody MsfEntity msfEntity) {
        int result = msfMapper.updateById(msfEntity);
        return result > 0 ? Result.success("更新成功") : Result.failure("更新失败");
    }

    // 删除渗透测试记录(管理员)
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(role = "admin")
    public Result deleteAttack(@PathVariable Integer id) {
        int result = msfMapper.deleteById(id);
        return result > 0 ? Result.success("删除成功") : Result.failure("删除失败");
    }

    // 条件查询渗透测试记录(管理员)
    @GetMapping("/search")
    @RequiresPermission(role = "admin")
    public Result searchAttacks(
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String vulnerabilityName,
            @RequestParam(required = false) String vulnerabilityModule,
            @RequestParam(required = false) Integer userId) {
        QueryWrapper<MsfEntity> queryWrapper = new QueryWrapper<>();
        if (ip != null) {
            queryWrapper.like("ip", ip);
        }
        if (vulnerabilityName != null) {
            queryWrapper.like("vulnerabilityName", vulnerabilityName);
        }
        if (vulnerabilityModule != null) {
            queryWrapper.like("vulnerabilityModule", vulnerabilityModule);
        }
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        List<MsfEntity> list = msfMapper.selectList(queryWrapper);
        return Result.success(list);
    }
}