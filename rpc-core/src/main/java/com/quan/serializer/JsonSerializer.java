package com.quan.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quan.entity.RpcRequest;
import com.quan.enumeration.SerializerCode;
import com.quan.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 使用 JSON 格式的序列化器
 * @author Quan
 */
public class JsonSerializer implements CommonSerializer {
    // 日志记录
    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    // 用于将Java对象转换为JSON(序列化)，或者将JSON转换为Java对象(反序列化)
    private ObjectMapper objectMapper = new ObjectMapper();

    // 将对象序列化为字节数组
    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch(JsonProcessingException e) {
            logger.error("序列化时有错误发生：", e);
            throw new SerializeException("序列化时有错误发生！");
        }
    }

    // 将字节数组反序列化为对象
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);
            if(obj instanceof RpcRequest) {
                obj = handleRequest(obj);
            }
            return obj;
        } catch(IOException e) {
            logger.error("反序列化时有错误发生：", e);
            throw new SerializeException("反序列化时有错误发生！");
        }
    }

    // 处理RpcRequest对象中的参数
    private Object handleRequest(Object obj) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) obj;
        for(int i = 0; i < rpcRequest.getParameters().length; i++) {
            Class<?> clazz = rpcRequest.getParamTypes()[i];
            if(!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }

    // 获取序列化器的编号
    @Override
    public int getCode() {
        return SerializerCode.valueOf("JSON").getCode();
    }
}
