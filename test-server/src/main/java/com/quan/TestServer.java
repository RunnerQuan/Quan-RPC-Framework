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
        String host = "0.0.0.0";
        int port = 0;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    return;
                case "-l":
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

        if (port == 0) {
            System.out.println("端口号不能为空");
            printHelp();
            return;
        }

        // 创建服务
        HelloService helloService = new HelloServiceImplementation();
        GoodbyeService goodbyeService = new GoodbyeServiceImplementation();

        // 注册中心的地址
        String registryHost = "192.168.1.10";
        int registryPort = 12350;

        // 创建RpcServer实例并添加服务
        RpcServer rpcServer = new RpcServer(host, port, "test-server", registryHost, registryPort);
        rpcServer.setSerializer(new JsonSerializer());
        rpcServer.publishService(helloService, HelloService.class.getCanonicalName());
        rpcServer.publishService(goodbyeService, GoodbyeService.class.getCanonicalName());
        rpcServer.start(); // 阻塞的方式启动服务，故所有服务的注册都要放在前面
    }

    private static void printHelp() {
        System.out.println("用法：");
        System.out.println("  -h                输出帮助信息");
        System.out.println("  -l <ip地址>       服务端监听的IP地址，默认监听所有地址");
        System.out.println("  -p <端口号>       服务端监听的端口号，不能为空");
    }
}