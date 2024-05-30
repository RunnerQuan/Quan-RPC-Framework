package com.quan;

import com.quan.server.RpcServer;

/**
 * 测试用服务提供方（服务端）
 * 用于启动服务提供方，注册服务
 * @author Quan
 */
public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImplementation();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 9000);
    }
}
