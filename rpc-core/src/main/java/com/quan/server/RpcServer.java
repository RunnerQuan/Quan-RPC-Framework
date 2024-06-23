package com.quan.server;

import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import com.quan.provider.ServiceProvider;
import com.quan.provider.ServiceProviderImplementation;
import com.quan.registry.QuanServiceRegistry;
import com.quan.registry.ServiceRegistry;
import com.quan.registry.ServiceRegistryClient;
import com.quan.serializer.CommonSerializer;
import com.quan.util.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * RPC 服务端类（远程方法调用的提供者）
 * 用于注册服务并监听客户端的连接
 * @author Quan
 */
public class RpcServer {
    // 用于记录与 RpcServer 相关的日志
    private final Logger logger = LoggerFactory.getLogger(RpcServer.class);
    private final ExecutorService threadPool; // 用于处理请求的线程池
    private final ServiceRegistry serviceRegistry; // 服务端的服务注册表（一个服务提供方可以提供多个服务）
    private final ServiceProvider serviceProvider; // 存储服务提供方
    private final RequestHandler requestHandler; // 用于处理请求
    private CommonSerializer serializer; // 序列化器
    private final String host; // 服务端的 IP 地址
    private final int port; // 服务端的端口号
    private final ServiceRegistryClient serviceRegistryClient; // 服务注册中心的客户端

    // 定时任务调度器；用于发送心跳包（保持连接）的线程池（定时任务线程池）
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // 构造函数
    public RpcServer(String host, int port, String serverName, String registryHost, int registryPort) {
        // 创建一个线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool(serverName);
        this.serviceRegistry = new QuanServiceRegistry();
        this.serviceProvider = new ServiceProviderImplementation();
        this.requestHandler = new RequestHandler(serviceProvider);
        this.host = host;
        this.port = port;
        this.serviceRegistryClient = new ServiceRegistryClient(registryHost, registryPort);
    }

    // 用于注册服务
    public void start() {
        if(serializer == null) {
            logger.error("序列化器未设置！");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务端已启动！");
            startHeartbeat(); // 启动心跳线程
            startServiceDownListener(); // 启动服务失活通知监听线程
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接：{}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown(); // 关闭线程池
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生：", e);
        }
    }

    // 启动心跳线程
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            for(Map.Entry<String, List<InetSocketAddress>> entry : serviceRegistry.getServiceMap().entrySet()) {
                String serviceName = entry.getKey();
                List<InetSocketAddress> addresses = entry.getValue();
                for(InetSocketAddress address : addresses) {
                    try {
//                        logger.info("发送心跳包，服务：{}，地址：{}", serviceName, address);
                        serviceRegistryClient.sendHeartbeat(serviceName, address);
                    } catch (IOException e) {
                        logger.error("心跳包发送失败，服务：{}", serviceName, e);
                        throw new RpcException(RpcError.HEARTBEAT_FAILURE);
                    }
                }
            }
        }, 0, 5, TimeUnit.SECONDS); // 每 3 秒发送一次心跳包
    }

    // 启动服务失活通知监听线程
    private void startServiceDownListener() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port + 1)) { // 使用不同的端口来接收服务失活通知
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                        String command = objectInputStream.readUTF();
                        if ("notify".equals(command)) {
                            String serviceName = objectInputStream.readUTF();
                            InetSocketAddress inetSocketAddress = (InetSocketAddress) objectInputStream.readObject();
                            handleServiceDeregister(serviceName, inetSocketAddress);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        logger.error("处理服务失活通知时发生错误：", e);
                    }
                }
            } catch (IOException e) {
                logger.error("启动服务失活通知监听线程时发生错误：", e);
            }
        }).start();
    }

    // 设置序列化器
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    // 发布服务
    public <T> void publishService(T service, String serviceName) {
        InetSocketAddress serviceAddress = new InetSocketAddress(host, port);
        try {
            serviceRegistryClient.register(serviceName, serviceAddress);
        } catch (IOException e) {
            logger.error("注册服务时有错误发生：", e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILURE);
        }
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, serviceAddress);
        logger.info("向接口: [{}] 注册服务: {}", service.getClass().getInterfaces()[0], service.getClass().getCanonicalName());
    }

    // 处理注册中心通知服务失活
    public void handleServiceDeregister(String serviceName, InetSocketAddress inetSocketAddress) {
        serviceRegistry.getServiceMap().get(serviceName).remove(inetSocketAddress);
        if (serviceRegistry.getServiceMap().get(serviceName).isEmpty()) {
            serviceRegistry.getServiceMap().remove(serviceName);
        }
        serviceProvider.removeServiceProvider(serviceName);
        logger.info("服务器：{} 删除失活服务：{}", inetSocketAddress, serviceName);
    }
}
