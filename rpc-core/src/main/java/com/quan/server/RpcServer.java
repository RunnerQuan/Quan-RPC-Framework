package com.quan.server;

import com.quan.registry.ServiceRegistry;

import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(RpcServer.class.getName());


    private static final int CORE_POOL_SIZE = 5; // 核心线程数5
    private static final int MAXIMUM_POOL_SIZE = 50; // 最大线程数50
    private static final int KEEP_ALIVE_TIME = 60; // 线程存活时间60s
    private static final int BLOCKING_QUEUE_CAPACITY = 100; // 阻塞队列容量100
    private final ExecutorService threadPool; // 用于处理请求的线程池
    private RequestHandler requestHandler = new RequestHandler(); // 用于处理请求
    private final ServiceRegistry serviceRegistry; // 服务注册表

    // 构造函数
    public RpcServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY); // 用于存放任务的阻塞队列
        ThreadFactory threadFactory = Executors.defaultThreadFactory(); // 创建线程的工厂
        // 创建线程池
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
    // 用于注册服务
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器正在启动...");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接！ IP为：" + socket.getInetAddress() + " 端口号为:" + socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry));
            }
        } catch (IOException e) {
            logger.severe("服务器启动时有错误发生：" + e);
        }
    }
}
