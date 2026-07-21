package com.example.penetration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@TableName("msf")
public class MsfEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("ip")
    private String ip;

    @TableField("user_id")
    private Integer userId;

    @TableField("vulnerabilityName")
    private String vulnerabilityName;

    @TableField("vulnerabilityModule")
    private String vulnerabilityModule;

    @TableField("process")
    private String process;

    @TableField("create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}