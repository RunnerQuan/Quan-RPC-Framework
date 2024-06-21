package com.quan;

import com.quan.registry.QuanServiceRegistry;
import com.quan.registry.ServiceRegistry;
import com.quan.registry.ServiceRegistryServer;

import java.io.IOException;

/**
 * 服务注册中心的启动类
 * 用于启动注册中心服务器
 * @author Quan
 */
public class ServiceRegistryServerMain {
    public static void main(String[] args) throws IOException {
        ServiceRegistry serviceRegistry = new QuanServiceRegistry();
        ServiceRegistryServer serviceRegistryServer = new ServiceRegistryServer(serviceRegistry);
        serviceRegistryServer.start(8080); // 启动注册中心服务器8080
    }
}
