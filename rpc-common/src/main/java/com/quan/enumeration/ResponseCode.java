package com.quan.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 方法调用的响应状态码
 * ResponseCode @author Quan
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {
    SUCCESS(200, "调用方法成功！"),
    FAIL(500, "调用方法失败！"),
    METHOD_NOT_FOUND(404, "未找到指定方法！"),
    CLASS_NOT_FOUND(404, "未找到指定类！");

    private final int code; // 状态码
    private final String message; // 状态码对应的信息
}
