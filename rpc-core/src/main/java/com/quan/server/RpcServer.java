package com.quan.server;

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
    /**
     * 用于处理请求的线程池
     */
    private final ExecutorService threadPool;

    /**
     * 用于记录与 RpcServer 相关的日志
     */
    private static final Logger logger = Logger.getLogger(RpcServer.class.getName());

    /**
     * 构造函数
     */
    public RpcServer() {
        int corePoolSize = 5; // 核心线程数5
        int maximumPoolSize = 50; // 最大线程数50
        long keepAliveTime = 60; // 线程存活时间60s
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    /**
     * 用于注册服务
     */
    public void register(Object service, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器正在启动...");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接！ IP为：" + socket.getInetAddress() + " 端口号为:" + socket.getPort());
                threadPool.execute(new RequestHandler(socket, service));
            }
        } catch (IOException e) {
            logger.severe("连接时有错误发生：" + e);
        }
    }
}
