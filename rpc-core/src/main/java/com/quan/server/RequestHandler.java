package com.quan.server;

import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;
import com.quan.enumeration.ResponseCode;
import com.quan.provider.ServiceProvider;
import com.quan.provider.ServiceProviderImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 进行过程调用的处理器
 * @author Quan
 */
public class RequestHandler {
    // 用于记录与 RequestHandler 相关的日志
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    // 服务提供方
    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new ServiceProviderImplementation();
    }

    // 处理请求
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    // 调用目标方法
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            logger.info("服务：{} 调用方法：{} 成功", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestID());
        }
        return result;
    }
}