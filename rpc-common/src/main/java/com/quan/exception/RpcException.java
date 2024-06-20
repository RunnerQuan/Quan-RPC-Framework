package com.quan.exception;

import com.quan.enumeration.RpcError;

/**
 * RPC调用异常
 * @author Quan
 */
public class RpcException extends RuntimeException {
    public RpcException(RpcError rpcError, String detail) {
        // super用于调用父类中的构造方法
        super(rpcError.getMessage() + ": " + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcError rpcError) {
        super(rpcError.getMessage());
    }
}
