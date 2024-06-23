package com.quan.provider;

import com.quan.exception.RpcException;
import com.quan.enumeration.RpcError;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的服务注册表，保存服务端本地服务
 * @author Quan
 */
public class ServiceProviderImplementation implements ServiceProvider {
    // 日志记录
    private final Logger logger = LoggerFactory.getLogger(ServiceProviderImplementation.class);
    // 服务注册表
    private final ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<>();
    // 用于存储已注册的服务
    private final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    // 注册服务
    @Override
    public <T> void addServiceProvider(T service, String serviceName) {
        if(registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);
        logger.info("向接口: {} 注册服务: {}", service.getClass().getInterfaces(), serviceName);
    }

    // 移除服务
    @Override
    public void removeServiceProvider(String serviceName) {
        registeredService.remove(serviceName);
        serviceMap.remove(serviceName);
        logger.info("从接口: {} 注销服务: {}", serviceName);
    }

    // 获取服务
    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if(service == null) {
            throw new RpcException(RpcError.SERVICE_NO_FOUND);
        }
        return service;
    }

    @Override
    public Set<String> getAllServiceNames() {
        return Collections.unmodifiableSet(registeredService);
    }
}
