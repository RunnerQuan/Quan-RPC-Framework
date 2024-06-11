package com.quan.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * RPC调用过程中的错误
 * RpcError
 * @author Quan
 */
@Getter
@AllArgsConstructor
public enum RpcError {
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_NO_FOUND("未找到服务"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务未实现接口");

    private final String message; // 错误信息
}
