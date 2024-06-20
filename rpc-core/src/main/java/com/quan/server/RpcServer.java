package com.quan.server;

import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import com.quan.registry.ServiceRegistry;
import com.quan.serializer.CommonSerializer;
import com.quan.util.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private RequestHandler requestHandler = new RequestHandler(); // 用于处理请求
    private final ServiceRegistry serviceRegistry; // 服务注册表
    private CommonSerializer serializer; // 序列化器

    // 构造函数
    public RpcServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        // 创建一个线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool("rpc-server");
    }

    // 用于注册服务
    public void start(int port) {
        if(serializer == null) {
            logger.error("序列化器未设置！");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器正在启动......");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接：IP为 {}，端口号为 {}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry, serializer));
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
}
