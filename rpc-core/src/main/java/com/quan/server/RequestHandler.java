package com.quan.server;

import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;
import com.quan.enumeration.ResponseCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * 进行过程调用的处理器
 * @author Quan
 */
public class RequestHandler {
    /**
     * 用于记录与 RequestHandler 相关的日志
     */
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("服务：" + rpcRequest.getInterfaceName() + " 成功调用方法：" + rpcRequest.getMethodName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.severe("调用或发送时有错误发生：" + e);
        }
        return result;
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws IllegalAccessException, InvocationTargetException {
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}