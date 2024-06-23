package com.quan;


import com.quan.serializer.JsonSerializer;
import com.quan.server.RpcServer;

/**
 * 测试用服务提供方（服务端）
 * 用于启动服务提供方，注册服务
 * @author Quan
 */
public class TestServer {
    public static void main(String[] args) {
        // 创建服务
        HelloService helloService = new HelloServiceImplementation();
        GoodbyeService goodbyeService = new GoodbyeServiceImplementation();

        // 注册中心的地址
        String registryHost = "127.0.0.1";
        int registryPort = 8848;

        // 创建RpcServer实例并添加服务
        RpcServer rpcServer = new RpcServer("127.0.0.1", 9999, "test-server", registryHost, registryPort);
        rpcServer.setSerializer(new JsonSerializer());
        rpcServer.publishService(helloService, HelloService.class.getCanonicalName());
        rpcServer.publishService(goodbyeService, GoodbyeService.class.getCanonicalName());
        rpcServer.start(); // 阻塞的方式启动服务，故所有服务的注册都要放在前面

//        // 多服务端测试
//        RpcServer rpcServer1 = new RpcServer("127.0.0.1", 9999, "test-server1", registryHost, registryPort);
//        rpcServer1.setSerializer(new JsonSerializer());
//        rpcServer1.publishService(helloService, HelloService.class.getCanonicalName());
//        rpcServer1.publishService(goodbyeService, GoodbyeService.class.getCanonicalName());
//        new Thread(rpcServer1::start).start(); // 启动服务端
//
//        RpcServer rpcServer2 = new RpcServer("127.0.0.1", 1145, "test-server2", registryHost, registryPort);
//        rpcServer2.setSerializer(new JsonSerializer());
//        rpcServer2.publishService(helloService, HelloService.class.getCanonicalName());
//        rpcServer2.publishService(goodbyeService, GoodbyeService.class.getCanonicalName());
//        new Thread(rpcServer2::start).start(); // 启动服务端
    }
}
