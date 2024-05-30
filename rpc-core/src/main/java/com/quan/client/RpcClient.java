package com.quan.client;

import com.quan.entity.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * RPC 客户端类（远程方法调用的消费者）
 * 用于发送请求到服务端并获取结果
 * @author Quan
 */
public class RpcClient {

    /**
     * 日志记录
     */
    private static final Logger logger = Logger.getLogger(RpcClient.class.getName());

    /**
     * 发送请求到服务端并获取结果
     * @param rpcRequest 请求对象
     * @param host 服务端主机地址
     * @param port 服务端端口
     * @return 服务端返回的结果
     */
    public Object sendRequest(RpcRequest rpcRequest,  String host, int port) {
        try (Socket socket = new Socket(host, port)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.severe("调用时发生错误：" + e);
            return null;
        }
    }
}
