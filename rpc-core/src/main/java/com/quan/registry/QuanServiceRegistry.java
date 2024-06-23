package com.quan.registry;

import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册表的自定义实现类
 * 用于注册服务和发现服务
 * @author Quan
 */
public class QuanServiceRegistry implements ServiceRegistry {
    private final Logger logger = LoggerFactory.getLogger(QuanServiceRegistry.class);

    private final ConcurrentHashMap<String, List<InetSocketAddress>> serviceMap = new ConcurrentHashMap<>();

    // 服务的最后心跳时间(键是 Service + InetSocketAddress.toString())
    private final ConcurrentHashMap<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();

    // 构造函数
    public QuanServiceRegistry() {
    }

    // 注册服务
    @Override
    public synchronized void register(String serviceName, InetSocketAddress inetSocketAddress) {
        if(serviceMap.containsKey(serviceName) && serviceMap.get(serviceName).contains(inetSocketAddress)) {
            logger.warn("已经注册过该服务：{}", serviceName);
            return;
        }
        serviceMap.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(inetSocketAddress);
        lastHeartbeatMap.put(serviceName + inetSocketAddress.toString(), System.currentTimeMillis());
        logger.info("服务器：{} 的服务：{} 注册进服务注册中心", inetSocketAddress, serviceName);
    }

    // 发现服务
    @Override
    public synchronized InetSocketAddress discoverService(String serviceName) {
        List<InetSocketAddress> addresses = serviceMap.get(serviceName); // 获取提供该服务的服务端列表
        if(addresses == null || addresses.isEmpty()) {
            logger.error("找不到对应的服务：{}", serviceName);
            throw new RpcException(RpcError.SERVICE_NO_FOUND);
        }
        // 使用随机选择策略实现负载均衡
        int randomIndex = new Random().nextInt(addresses.size());
        InetSocketAddress address = addresses.get(randomIndex); // 随机选择一个服务端
        logger.info("发现服务：{} 的地址为：{}", serviceName, address);
        return address;
    }

    // 处理心跳包，更新服务的最后心跳时间
    public synchronized void heartbeat(String serviceName, InetSocketAddress inetSocketAddress) {
        String serviceKey = buildServiceKey(serviceName, inetSocketAddress);
        if (serviceMap.containsKey(serviceName) && serviceMap.get(serviceName).contains(inetSocketAddress)) {
            lastHeartbeatMap.put(serviceKey, System.currentTimeMillis());
//            logger.info("收到服务：{} 的心跳包", serviceName);
        } else {
            register(serviceName, inetSocketAddress);
            logger.info("未注册服务：{}，进行注册", serviceName);
        }
    }

    // 获取心跳列表
    @Override
    public ConcurrentHashMap<String, Long> getLastHeartbeatMap() {
        return lastHeartbeatMap;
    }

    @Override
    // 获取服务列表
    public ConcurrentHashMap<String, List<InetSocketAddress>> getServiceMap() {
        return serviceMap;
    }

    private String buildServiceKey(String serviceName, InetSocketAddress inetSocketAddress) {
        return serviceName + inetSocketAddress.toString();
    }
}