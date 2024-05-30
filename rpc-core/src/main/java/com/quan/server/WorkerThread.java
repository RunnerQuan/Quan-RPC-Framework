package com.quan.server;

import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.logging.Logger;

public class WorkerThread implements Runnable {
    /**
     * 用于记录与 WorkerThread 相关的日志
     */
    private static final Logger logger = Logger.getLogger(WorkerThread.class.getName());

    /**
     * 用于处理客户端的请求并将处理结果返回给客户端
     */
    private Socket socket;

    /**
     * 服务对象
     */
    private Object service;

    /**
     * 构造函数
     */
    public WorkerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    /**
     * 用于处理客户端的请求并将处理结果返回给客户端
     */
    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object returnObject = method.invoke(service, rpcRequest.getParameters());
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.severe("调用或发送时有错误发生：" + e);
        }
    }
}