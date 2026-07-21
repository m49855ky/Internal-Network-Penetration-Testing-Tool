package com.example.penetration.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@TableName("portscan")
public class PortScanEntity {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    @TableField("ip")
    private String ip;
    @TableField("port")
    private Integer port;
    @TableField("status")
    private String status;
    @TableField("user_id")
    private Integer userId;
    @TableField("protocol")
    private String protocol;
    @TableField("type")
    private String type;
    @TableField("create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime createTime;
}
