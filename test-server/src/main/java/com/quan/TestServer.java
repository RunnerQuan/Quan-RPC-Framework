package com.quan;

import com.quan.provider.ServiceProviderImplementation;
import com.quan.registry.ServiceRegistry;
import com.quan.registry.ServiceRegistryClient;
import com.quan.serializer.JsonSerializer;
import com.quan.server.RpcServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 测试用服务提供方（服务端）
 * 用于启动服务提供方，注册服务
 * @author Quan
 */
public class TestServer {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // 创建服务
        HelloService helloService = new HelloServiceImplementation();
        GoodbyeService goodbyeService = new GoodbyeServiceImplementation();

        // 注册中心的地址
        String registryHost = "127.0.0.1";
        int registryPort = 8080;

        // 创建RpcServer实例并添加服务
        RpcServer rpcServer = new RpcServer("127.0.0.1", 9000, registryHost, registryPort);
        rpcServer.setSerializer(new JsonSerializer());
        rpcServer.publishService(helloService, HelloService.class.getCanonicalName());
        rpcServer.publishService(goodbyeService, GoodbyeService.class.getCanonicalName());
        rpcServer.start(); // 阻塞的方式启动服务，故所有服务的注册都要放在前面
    }
}
