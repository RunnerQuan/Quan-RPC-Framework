package com.quan.server;

import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import com.quan.provider.ServiceProvider;
import com.quan.provider.ServiceProviderImplementation;
import com.quan.registry.QuanServiceRegistry;
import com.quan.registry.ServiceRegistry;
import com.quan.serializer.CommonSerializer;
import com.quan.util.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * RPC 服务端类（远程方法调用的提供者）
 * 用于注册服务并监听客户端的连接
 * @author Quan
 */
public class RpcServer {
    // 用于记录与 RpcServer 相关的日志
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);
    private final ExecutorService threadPool; // 用于处理请求的线程池
    private final RequestHandler requestHandler = new RequestHandler(); // 用于处理请求
    private final ServiceRegistry serviceRegistry; // 本服务端的服务注册表（一个服务提供方可以提供多个服务）
    private final ServiceProvider serviceProvider; // 存储服务提供方
    private CommonSerializer serializer; // 序列化器
    private final String host; // 服务端的 IP 地址
    private final int port; // 服务端的端口号
    // 构造函数
    public RpcServer(String host, int port) {
        // 创建一个线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool("rpc-server");
        this.serviceRegistry = new QuanServiceRegistry();
        this.serviceProvider = new ServiceProviderImplementation();
        this.host = host;
        this.port = port;
    }

    // 用于注册服务
    public void start() {
        if(serializer == null) {
            logger.error("序列化器未设置！");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器已启动！");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接：IP为 {}，端口号为 {}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown(); // 关闭线程池
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生：", e);
        }
    }

    // 设置序列化器
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    // 发布服务
    public <T> void publishService(T service, String serviceName) {
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }
}
