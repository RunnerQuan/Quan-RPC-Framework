package com.quan.client;

import com.quan.exception.RpcException;
import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;
import com.quan.enumeration.ResponseCode;
import com.quan.enumeration.RpcError;
import com.quan.serializer.CommonSerializer;
import com.quan.util.ObjectReader;
import com.quan.util.ObjectWriter;
import com.quan.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * RPC 客户端类（远程方法调用的消费者）
 * 用于发送请求到服务端并获取结果
 * @author Quan
 */
public class RpcClient {

    // 日志记录
    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    // 服务端主机地址
    private final String host;
    // 服务端端口
    private final int port;

    // 序列化器
    private CommonSerializer serializer;

    // 连接超时时间
    private final int connectTimeout;
    // 读取超时时间
    private final int readTimeout;

    // 构造函数
    public RpcClient(String host, int port) {
        // 默认超时配置：连接超时时间为 3 秒，读取超时时间为 10 秒
        this(host, port, 3000, 10000);
        logger.info("随机调度：({}, {})", host, port);
    }

    public RpcClient(String host, int port, int connectTimeout, int readTimeout) {
        this.host = host;
        this.port = port;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    // 设置序列化器
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 发送请求到服务端并获取结果
     * @param rpcRequest 请求对象
     * @return 服务端返回的结果
     */
    public Object sendRequest(RpcRequest rpcRequest) {
        if(serializer == null) {
            logger.error("序列化器未设置！");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), connectTimeout);
            socket.setSoTimeout(readTimeout);
            try(OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream()) {

                ObjectWriter.writeObject(outputStream, rpcRequest, serializer);
                Object obj = ObjectReader.readObject(inputStream);
                RpcResponse rpcResponse = (RpcResponse) obj;

                if (rpcResponse == null) {
                    logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                    throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" +
                            rpcRequest.getInterfaceName());
                }

                if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() !=
                        ResponseCode.SUCCESS.getCode()) {
                    logger.error("服务调用失败，service：{}，response：{}", rpcRequest.getInterfaceName(),
                            rpcResponse);
                    throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" +
                            rpcRequest.getInterfaceName());
                }

                // 检查响应与请求是否匹配
                RpcMessageChecker.check(rpcRequest, rpcResponse);
                return rpcResponse.getData();
            } catch (SocketTimeoutException e) {
                logger.error("读写数据超时：", e);
                throw new RpcException(RpcError.IO_TIMEOUT);
            } catch (IOException e) {
                logger.error("读写数据时发生错误：", e);
                throw new RpcException(RpcError.IO_ERROR);
            }
        } catch (SocketTimeoutException e) {
            logger.error("连接超时：", e);
            throw new RpcException(RpcError.SOCKET_TIMEOUT);
        } catch (IOException e) {
            logger.error("连接时发生错误：", e);
            throw new RpcException(RpcError.SOCKET_ERROR);
        }
    }
}
