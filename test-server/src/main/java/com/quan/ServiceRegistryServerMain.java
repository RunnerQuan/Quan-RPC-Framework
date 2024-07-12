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
        // 注册中心的IP和端口号直接固定为 192.168.1.10:12350
        ServiceRegistryServer serviceRegistryServer = new ServiceRegistryServer();
        serviceRegistryServer.start(12350); // 启动注册中心服务器12350
    }
}