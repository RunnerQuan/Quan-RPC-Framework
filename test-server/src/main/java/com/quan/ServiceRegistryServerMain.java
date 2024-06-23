package com.quan;

import com.quan.registry.ServiceRegistryServer;

import java.io.IOException;

/**
 * 服务注册中心的启动类
 * 用于启动注册中心服务器
 * @author Quan
 */
public class ServiceRegistryServerMain {
    public static void main(String[] args) throws IOException {
        ServiceRegistryServer serviceRegistryServer = new ServiceRegistryServer();
        serviceRegistryServer.start(8848); // 启动注册中心服务器8080
    }
}
