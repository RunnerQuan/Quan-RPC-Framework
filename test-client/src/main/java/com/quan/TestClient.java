package com.quan;

import com.quan.client.RpcClientProxy;

/**
 * 测试用客户端
 * 用于启动客户端，调用服务提供方的方法
 */
public class TestClient {
    public static void main(String[] args) {
         RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 6666);
         HelloService helloService = proxy.getProxy(HelloService.class);
         HelloObject object = new HelloObject(618, "This is a message");
         String res = helloService.hello(object);
         System.out.println(res);
    }
}
