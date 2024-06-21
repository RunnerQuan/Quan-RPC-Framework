package com.quan.provider;

/**
 * 保存和提供服务实例对象
 * @author Quan
 */
public interface ServiceProvider {

    // 添加服务提供方(生产者)
    <T> void addServiceProvider(T service, String serviceName);

    // 获取服务提供方(生产者)
    Object getServiceProvider(String serviceName);
}
