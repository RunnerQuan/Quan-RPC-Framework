package com.quan.provider;

import java.util.Set;

/**
 * 保存和提供服务实例对象
 * @author Quan
 */
public interface ServiceProvider {

    // 添加服务提供方(生产者)
    <T> void addServiceProvider(T service, String serviceName);

    void removeServiceProvider(String serviceName);

    // 获取服务提供方(生产者)
    Object getServiceProvider(String serviceName);

    // 获取能提供的所有服务名称
    Set<String> getAllServiceNames();
}
