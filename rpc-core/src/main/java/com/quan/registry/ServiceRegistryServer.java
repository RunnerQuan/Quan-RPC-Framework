package com.quan.registry;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 注册中心服务器
 * @author Quan
 */
public class ServiceRegistryServer {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryServer.class);

    private final ServiceRegistry serviceRegistry;

    public ServiceRegistryServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void start(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        logger.info("注册中心服务端已启动！端口号为: {}", port);
        while(true) {
            Socket socket = serverSocket.accept();
            new Thread(new ServiceRegistryServerHandler(socket)).start();
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
                if ("register".equals(command)) {
                    String serviceName = objectInputStream.readUTF();
                    InetSocketAddress inetSocketAddress = (InetSocketAddress) objectInputStream.readObject();
                    serviceRegistry.register(serviceName, inetSocketAddress);
                    objectOutputStream.writeUTF("Service registered successfully");
                } else if ("discover".equals(command)) {
                    String serviceName = objectInputStream.readUTF();
                    InetSocketAddress address = serviceRegistry.discoverService(serviceName);
                    objectOutputStream.writeObject(address);
                } else {
                    objectOutputStream.writeUTF("Unknown command");
                }
                objectOutputStream.flush();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
