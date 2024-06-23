package com.quan.registry;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 注册中心服务器
 * @author Quan
 */
public class ServiceRegistryServer {
    private final static Logger logger = LoggerFactory.getLogger(ServiceRegistryServer.class);

    // 全局服务注册表(只有一个注册中心)
    private static ServiceRegistry serviceRegistry;

    // 服务失活阈值，单位为毫秒
    private static final long SERVICE_TIMEOUT = 15 * 1000;

    public ServiceRegistryServer() {
        serviceRegistry = new QuanServiceRegistry();
    }

    public void start(int port) throws IOException {
        // 启动定期检查服务状态的任务
        startHeartbeatCheck();
        ServerSocket serverSocket = new ServerSocket(port);
        logger.info("注册中心服务端已启动！端口号为: {}", port);
        while(true) {
            Socket socket = serverSocket.accept();
            new Thread(new ServiceRegistryServerHandler(socket)).start();
        }
    }

    // 启动定时任务，定期检查服务状态
    private synchronized void startHeartbeatCheck() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkServices, 0, 10, TimeUnit.SECONDS);
    }

    // 检查服务状态
    private synchronized void checkServices() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : serviceRegistry.getLastHeartbeatMap().entrySet()) {
            String key = entry.getKey();
            long lastHeartbeatTime = entry.getValue();
            if (currentTime - lastHeartbeatTime > SERVICE_TIMEOUT) {
                String serviceName = key.substring(0, key.indexOf("/"));
                InetSocketAddress inetSocketAddress = new InetSocketAddress(key.substring(key.indexOf("/") + 1, key.indexOf(":")), Integer.parseInt(key.substring(key.indexOf(":") + 1)));

                // 通知所有服务端删除失活服务（要先通知服务端删除失活服务再更新注册中心的注册表）
                notifyServiceDown(serviceName, inetSocketAddress);

                serviceRegistry.getServiceMap().get(serviceName).remove(inetSocketAddress);
                serviceRegistry.getLastHeartbeatMap().remove(key);
                logger.info("服务器：{} 的服务：{} 已失活，被移除服务注册中心", inetSocketAddress, serviceName);
                // 特判移除后服务列表为空的情况
                if (serviceRegistry.getServiceMap().get(serviceName).isEmpty()) {
                    serviceRegistry.getServiceMap().remove(serviceName);
                    logger.info("服务：{} 均已从服务注册中心移除！", serviceName);
                }
            }
        }
    }

    private void notifyServiceDown(String serviceName, InetSocketAddress inetSocketAddress) {
        // 通知所有服务端删除失活服务
        for (InetSocketAddress address : serviceRegistry.getServiceMap().get(serviceName)) {
            try (Socket socket = new Socket(address.getHostName(), address.getPort());
                 OutputStream outputStream = socket.getOutputStream();
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
                objectOutputStream.writeUTF("notify");
                objectOutputStream.writeUTF(serviceName);
                objectOutputStream.writeObject(inetSocketAddress);
                objectOutputStream.flush();
                logger.info("通知服务端删除失活服务成功" + address.getAddress());
            } catch (IOException e) {
                if(isServerDown(e)) {
                    if (isServerDown(e)) {
                        logger.warn("无法连接到服务器: {}. 服务器可能已停机", address.getAddress());
                        handleServerShutdown(address);
                    } else {
                        logger.error("通知服务端删除失活服务时发生错误：", e);
                    }
                }
            }
        }
    }

    // 判断服务器是否停机的辅助方法
    private boolean isServerDown(IOException e) {
        return e.getMessage().contains("Connection refused") || e.getMessage().contains("Connection reset");
    }

    // 处理服务器停机的辅助方法
    private void handleServerShutdown(InetSocketAddress address) {
        for (Map.Entry<String, List<InetSocketAddress>> entry : serviceRegistry.getServiceMap().entrySet()) {
            List<InetSocketAddress> addresses = entry.getValue();
            if (addresses.contains(address)) {
                addresses.remove(address);
                logger.info("服务器：{} 的服务：{} 已失活，被移除服务注册中心", address, entry.getKey());
                if (addresses.isEmpty()) {
                    serviceRegistry.getServiceMap().remove(entry.getKey());
                    logger.info("服务：{} 均已从服务注册中心移除！", entry.getKey());
                }
            }
        }
    }

    @AllArgsConstructor
    private class ServiceRegistryServerHandler implements Runnable {
        private final Socket socket;

        @Override
        public void run() {
            try (InputStream inputStream = socket.getInputStream();
                 OutputStream outputStream = socket.getOutputStream();
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

                String command = objectInputStream.readUTF();
                switch (command) {
                    case "register": {
                        String serviceName = objectInputStream.readUTF();
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) objectInputStream.readObject();
                        serviceRegistry.register(serviceName, inetSocketAddress);
                        objectOutputStream.writeUTF("Service registered successfully");
                        break;
                    }
                    case "discover": {
                        String serviceName = objectInputStream.readUTF();
                        InetSocketAddress address = serviceRegistry.discoverService(serviceName);
                        objectOutputStream.writeObject(address);
                        break;
                    }
                    case "heartbeat": {
                        String serviceName = objectInputStream.readUTF();
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) objectInputStream.readObject();
                        // 更新服务心跳时间
                        ((QuanServiceRegistry) serviceRegistry).heartbeat(serviceName, inetSocketAddress);
                        objectOutputStream.writeUTF("Heartbeat received");
                        break;
                    }
                    case "notify": {
                        String serviceName = objectInputStream.readUTF();
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) objectInputStream.readObject();
                        // 通知服务端删除失活服务
                        notifyServiceDown(serviceName, inetSocketAddress);
                        objectOutputStream.writeUTF("Service down notification sent");
                        break;
                    }
                    default:
                        objectOutputStream.writeUTF("Unknown command");
                        break;
                }
                objectOutputStream.flush();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("调用或处理时有错误发生：", e);
            }
        }
    }
}
