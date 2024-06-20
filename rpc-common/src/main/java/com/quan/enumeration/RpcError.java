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
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务未实现接口"),
    UNKNOWN_ERROR("出现未知错误"),
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务端失败"),
    UNKNOWN_PROTOCOL("未知的协议包"),
    UNKNOWN_SERIALIZER("未知的序列化器"),
    UNKNOWN_DESERIALIZER("未知的反序列化器"),
    UNKNOWN_PACKAGE_TYPE("未知的数据包类型"),
    RESPONSE_NOT_MATCH("响应请求号与请求号不匹配"),
    FAILED_TO_CONNECT_TO_SERVICE_REGISTRY("连接注册中心失败"),
    REGISTER_SERVICE_FAILURE("注册服务失败"),
    SERIALIZER_NOT_FOUND("未知的序列化器");
    private final String message; // 错误信息
}
