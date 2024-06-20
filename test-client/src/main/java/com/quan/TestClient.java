package com.quan;

import com.quan.client.RpcClient;
import com.quan.client.RpcClientProxy;
import com.quan.serializer.JsonSerializer;

/**
 * 测试用客户端
 * 用于启动客户端，调用服务提供方的方法
 */
public class TestClient {
    public static void main(String[] args) {
        // 127.0.0.1 本地回环地址
        RpcClient rpcClient = new RpcClient("127.0.0.1", 9000);
        rpcClient.setSerializer(new JsonSerializer());
        RpcClientProxy proxy = new RpcClientProxy(rpcClient);
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(618, "This is a message!");
        String res = helloService.hello(object);
        System.out.println(res);
    }
}
