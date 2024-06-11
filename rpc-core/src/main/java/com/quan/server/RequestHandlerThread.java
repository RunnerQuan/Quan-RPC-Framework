package com.quan.server;

import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;
import com.quan.registry.ServiceRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * 处理RpcRequest的工作线程
 * @author Quan
 */
public class RequestHandlerThread implements Runnable {
    // 用于记录与 RequestHandlerThread 相关的日志
    private static final Logger logger = Logger.getLogger(RequestHandlerThread.class.getName());

    // 与客户端建立的socket连接
    private Socket socket;
    // 用于处理RpcRequest
    private RequestHandler requestHandler;
    // 服务注册表，用于查找服务
    private ServiceRegistry serviceRegistry;

    // 构造函数
    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceRegistry serviceRegistry) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
    }

    // 重写run方法，处理RpcRequest
    @Override
    public void run() {
        // 使用try-with-resources确保操作完成后socket能够正常关闭
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = requestHandler.handle(rpcRequest, service);
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.severe("调用或发送时有错误发生：" + e);
        }
    }
}
