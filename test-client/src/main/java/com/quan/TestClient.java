package com.quan;

import com.quan.client.RpcClient;
import com.quan.client.RpcClientProxy;
import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import com.quan.registry.ServiceRegistryClient;
import com.quan.serializer.JsonSerializer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 测试用客户端
 * 用于启动客户端，调用服务提供方的方法
 */
public class TestClient {
    public static void main(String[] args) throws IOException {
        // 从服务注册中心查找服务地址  127.0.0.1 本地回环地址
        ServiceRegistryClient serviceRegistryClient = new ServiceRegistryClient("127.0.0.1", 8848);
        InetSocketAddress serviceAddress_hello = serviceRegistryClient.discover(HelloService.class.getCanonicalName());
        InetSocketAddress serviceAddress_goodbye = serviceRegistryClient.discover(GoodbyeService.class.getCanonicalName());

        if(serviceAddress_hello == null) {
            throw new RpcException(RpcError.SERVICE_NO_FOUND);
        }

        if(serviceAddress_goodbye == null) {
            throw new RpcException(RpcError.SERVICE_NO_FOUND);
        }

        // 创建RpcClient并连接到服务地址
        RpcClient rpcClient_hello = new RpcClient(serviceAddress_hello.getHostName(), serviceAddress_hello.getPort());
        rpcClient_hello.setSerializer(new JsonSerializer());
        RpcClient rpcClient_goodbye = new RpcClient(serviceAddress_goodbye.getHostName(), serviceAddress_goodbye.getPort());
        rpcClient_goodbye.setSerializer(new JsonSerializer());

        // 使用代理调用服务
        RpcClientProxy rpcClientProxy_hello = new RpcClientProxy(rpcClient_hello);
        HelloService helloService = rpcClientProxy_hello.getProxy(HelloService.class);
        HelloObject object = new HelloObject(618, "This is a message!");
        System.out.println(helloService.hello(object));

        RpcClientProxy rpcClientProxy_goodbye = new RpcClientProxy(rpcClient_goodbye);
        GoodbyeService byeService = rpcClientProxy_goodbye.getProxy(GoodbyeService.class);
        System.out.println(byeService.goodbye("RPC"));
    }
}
