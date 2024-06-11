package com.quan.registry;

/**
 * 服务注册表通用接口
 * @author Quan
 */
public interface ServiceRegistry {

    /**
     * 注册服务
     * @param service 待注册的服务实体
     * @param <T> 服务实体类
     */
    <T> void register(T service);

    /**
     * 获取服务
     * @param serviceName 服务名称
     * @return 服务实体
     */
    Object getService(String serviceName);
}
