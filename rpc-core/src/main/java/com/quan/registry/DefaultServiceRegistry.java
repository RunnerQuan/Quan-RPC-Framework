package com.quan.registry;

import com.quan.RpcException.RpcException;
import com.quan.enumeration.RpcError;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 默认的服务注册表，保存服务端本地服务实例
 * @author Quan
 */
public class DefaultServiceRegistry implements ServiceRegistry {
    // 日志记录
    private static final Logger logger = Logger.getLogger(DefaultServiceRegistry.class.getName());

    // 服务注册表
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    // 用于存储已注册的服务
    private final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    // 注册服务
    @Override
    public synchronized <T> void register(T service) {
        String serviceName = service.getClass().getName();
        if(registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if(interfaces.length == 0) {
            throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for(Class<?> i : interfaces) {
            // getCanonicalName() 返回类的规范名称(包括包名和类名)
            serviceMap.put(i.getCanonicalName(), service);
        }
        logger.info("向接口: {} 注册服务: {}" + interfaces + serviceName);
    }

    // 获取服务
    @Override
    public synchronized Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if(service == null) {
            throw new RpcException(RpcError.SERVICE_NO_FOUND);
        }
        return service;
    }
}
