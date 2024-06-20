package com.quan.entity;

import com.quan.enumeration.ResponseCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 提供者执行完成或出错后向消费者返回的结果对象
 * RPC Response @author Quan
 */
@Data
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {
    // 响应对应的请求号
    private String requestID;

    // 响应状态码
    private Integer statusCode;

    // 响应状态补充信息
    private String message;

    // 响应数据
    private T data;

    // 响应成功
    public static <T> RpcResponse<T> success(T data, String requestID) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestID(requestID);
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    // 响应失败
    public static <T> RpcResponse<T> fail(ResponseCode code, String requestID) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestID(requestID);
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }
}
