package com.quan;

import com.quan.client.RpcClient;
import com.quan.client.RpcClientProxy;
import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import com.quan.registry.ServiceRegistryClient;
import com.quan.serializer.JsonSerializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试用客户端
 * 用于启动客户端，调用服务提供方的方法
 */
public class TestClient {
    public static void main(String[] args) throws IOException {
        String host = null;
        int port = 0;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    return;
                case "-i":
                    if (i + 1 < args.length) {
                        host = args[i + 1];
                        i++;
                    } else {
                        System.out.println("缺少参数值：" + args[i]);
                        printHelp();
                        return;
                    }
                    break;
                case "-p":
                    if (i + 1 < args.length) {
                        port = Integer.parseInt(args[i + 1]);
                        i++;
                    } else {
                        System.out.println("缺少参数值：" + args[i]);
                        printHelp();
                        return;
                    }
                    break;
                default:
                    System.out.println("未知参数：" + args[i]);
                    printHelp();
                    return;
            }
        }

        if (host == null || port == 0) {
            System.out.println("IP地址和端口号不能为空");
            printHelp();
            return;
        }

        // 从服务注册中心查找服务地址
        ServiceRegistryClient serviceRegistryClient = new ServiceRegistryClient("192.168.1.10", 12350);
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
        System.out.println(byeService.goodbye("Quan-RPC"));
    }

    private static void printHelp() {
        System.out.println("用法：");
        System.out.println("  -h                输出帮助信息");
        System.out.println("  -i <ip地址>       客户端需要发送的服务端IP地址，不能为空");
        System.out.println("  -p <端口号>       客户端需要发送的服务端端口号，不能为空");
    }
}
