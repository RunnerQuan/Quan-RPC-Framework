package com.quan.util;

import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;
import com.quan.enumeration.ResponseCode;
import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查响应和请求
 * @author Quan
 */
public class RpcMessageChecker {
    public static final String INTERFACE_NAME = "interfaceName";
    private static final Logger logger = LoggerFactory.getLogger(RpcMessageChecker.class);

    private RpcMessageChecker() {}

    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        if(rpcResponse == null) {
            logger.error("调用服务失败，serviceName：{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + "：" + rpcRequest.getInterfaceName());
        }
        if(!rpcRequest.getRequestID().equals(rpcResponse.getRequestID())) {
            logger.error("调用服务失败，serviceName：{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.RESPONSE_NOT_MATCH, INTERFACE_NAME + "：" + rpcRequest.getInterfaceName());
        }
        if(rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode())) {
            logger.error("调用服务失败，serviceName：{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + "：" + rpcRequest.getInterfaceName());
        }
    }
}
