package com.quan.server;

import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;
import com.quan.registry.ServiceRegistry;
import com.quan.serializer.CommonSerializer;
import com.quan.util.ObjectReader;
import com.quan.util.ObjectWriter;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * 处理RpcRequest的工作线程
 * @author Quan
 */
//@AllArgsConstructor
public class RequestHandlerThread implements Runnable {
    // 用于记录与 RequestHandlerThread 相关的日志
    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);
    // 与客户端建立的socket连接
    private Socket socket;
    // 用于处理RpcRequest
    private RequestHandler requestHandler;
    // 序列化器
    private CommonSerializer serializer;

    // 构造函数
    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serializer = serializer;
    }

    // 重写run方法，处理RpcRequest
    @Override
    public void run() {
        // 使用try-with-resources确保操作完成后socket能够正常关闭
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
            Object result = requestHandler.handle(rpcRequest);
            RpcResponse<Object> response = RpcResponse.success(result, rpcRequest.getRequestID());
            ObjectWriter.writeObject(outputStream, response, serializer);
        } catch (IOException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }
}
