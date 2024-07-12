package com.quan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消费者向提供者发送的请求对象
 * RpcRequest @author Quan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {
    // 请求号，用于区分不同的请求
    private String requestID;

    // 待调用接口名称
    private String interfaceName;

    // 待调用方法名称
    private String methodName;

    // 调用方法的参数
    private Object[] parameters;

    // 调用方法的参数类型
    private Class<?>[] paramTypes;
}
