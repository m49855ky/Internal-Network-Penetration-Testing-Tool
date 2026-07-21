package com.example.penetration.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code;
    private String message;
    private Object data;

    public static Result success() {
        return new Result(200, "success", null);
    }  //返回成功结果，不带数据

    public static Result success(Object data) {
        return new Result(200, "success", data);
    }   //返回成功结果，带数据

    public static Result failure(String message) {
        return new Result(500, message, null);
    }   //返回失败结果
}