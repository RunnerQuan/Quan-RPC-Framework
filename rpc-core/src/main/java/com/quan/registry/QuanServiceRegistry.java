package com.quan.registry;

import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册表的默认实现类
 * 用于注册服务和发现服务
 * @author Quan
 */
public class QuanServiceRegistry implements ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(QuanServiceRegistry.class);

    // 注册中心的服务列表
    private static final ConcurrentHashMap<String, InetSocketAddress> serviceMap = new ConcurrentHashMap<>();

    // 注册服务
    @Override
    public synchronized void register(String serviceName, InetSocketAddress inetSocketAddress) {
        if(serviceMap.containsKey(serviceName)) {
            logger.error("已经注册过该服务：{}", serviceName);
            return;
        }
        serviceMap.put(serviceName, inetSocketAddress);
        logger.info("服务：{} 注册进注册表", serviceName);
    }

    // 发现服务
    @Override
    public InetSocketAddress discoverService(String serviceName) {
        InetSocketAddress address = serviceMap.get(serviceName);
        if(address == null) {
            logger.error("找不到对应的服务：{}", serviceName);
            throw new RpcException(RpcError.SERVICE_NO_FOUND);
        }
        logger.info("发现服务：{} 的地址为：{}", serviceName, address);
        return address;
    }
}
