package com.quan.server;

import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;
import com.quan.enumeration.ResponseCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * 用于处理客户端的请求并将处理结果返回给客户端
 */
public class RequestHandler implements Runnable {
    /**
     * 用于记录与 RequestHandler 相关的日志
     */
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

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
    public RequestHandler(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    /**
     * 用于处理客户端的请求并将处理结果返回给客户端
     */
    @Override
    public void run() {
        // 使用try-with-resources确保操作完成后socket能够正常关闭
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object returnObject = invokeMethod(rpcRequest); // 反射调用本地服务
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            logger.severe("调用或发送时有错误发生：" + e);
        }
    }

    /**
     * 反射调用本地服务
     * @param rpcRequest
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    private Object invokeMethod(RpcRequest rpcRequest) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Class<?> clazz = Class.forName(rpcRequest.getInterfaceName());
        if(!clazz.isAssignableFrom((service.getClass()))) {
            return RpcResponse.fail(ResponseCode.CLASS_NOT_FOUND);
        }
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }
}