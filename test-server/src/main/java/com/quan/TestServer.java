package com.quan;

import com.quan.registry.DefaultServiceRegistry;
import com.quan.registry.ServiceRegistry;
import com.quan.serializer.JsonSerializer;
import com.quan.server.RpcServer;

/**
 * 测试用服务提供方（服务端）
 * 用于启动服务提供方，注册服务
 * @author Quan
 */
public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImplementation(); // 创建服务
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry(); // 创建服务注册中心
        serviceRegistry.register(helloService); // 注册服务
        RpcServer rpcServer = new RpcServer(serviceRegistry); // 创建服务端
        rpcServer.setSerializer(new JsonSerializer());
        rpcServer.start(9000); // 启动服务端
    }
}
