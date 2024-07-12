package com.quan.registry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册表通用接口
 * @author Quan
 */
public interface ServiceRegistry {

    /**
     * 将一个服务注册进注册表
     * @param serviceName 服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    /**
     * 根据服务名获取服务实体
     * @param serviceName 服务名称
     * @return 服务实体
     */
    InetSocketAddress discoverService(String serviceName);

    /**
     * 获取心跳列表
     * @return 心跳列表
     */
    ConcurrentHashMap<String, Long> getLastHeartbeatMap();

    /**
     * 获取服务列表
     * @return 服务列表
     */
    ConcurrentHashMap<String, List<InetSocketAddress>> getServiceMap();


}
