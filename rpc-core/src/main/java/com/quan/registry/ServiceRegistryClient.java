package com.quan.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 服务注册中心的客户端类
 * 用于向注册中心注册服务和从注册中心发现服务
 * @author Quan
 */
public class ServiceRegistryClient {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryClient.class);
    // 注册中心的地址
    private final String host;
    // 注册中心的端口
    private final int port;

    public ServiceRegistryClient(String host, int port) {
        this.host = host;
        this.port = port;
//        logger.info("注册中心客户端创建成功！注册中心地址为：/{}:{}", host, port);
    }

    // 注册服务
    public void register(String serviceName, InetSocketAddress inetSocketAddress) throws IOException {
        try (Socket socket = new Socket(host, port);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            objectOutputStream.writeUTF("register");
            objectOutputStream.writeUTF(serviceName);
            objectOutputStream.writeObject(inetSocketAddress);
            objectOutputStream.flush();

            String response = objectInputStream.readUTF();
//            logger.info("Response from server: [{}]", response);
        }
    }

    // 发现服务
    public InetSocketAddress discover(String serviceName) throws IOException {
        try (Socket socket = new Socket(host, port);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            objectOutputStream.writeUTF("discover");
            objectOutputStream.writeUTF(serviceName);
            objectOutputStream.flush();

            InetSocketAddress inetSocketAddress = (InetSocketAddress) objectInputStream.readObject();
//            logger.info("Service address discovered: [{}]", inetSocketAddress);
            return inetSocketAddress;
        } catch (ClassNotFoundException e) {
            logger.error("反序列化时产生错误！", e);
            return null;
        }
    }

    // 发送心跳包
    public void sendHeartbeat(String serviceName, InetSocketAddress inetSocketAddress) throws IOException {
        try (Socket socket = new Socket(host, port);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            objectOutputStream.writeUTF("heartbeat");
            objectOutputStream.writeUTF(serviceName);
            objectOutputStream.writeObject(inetSocketAddress);
            objectOutputStream.flush();

            String response = objectInputStream.readUTF();
//            logger.info("心跳包发送成功，响应：{}", response);
        }
    }
}
