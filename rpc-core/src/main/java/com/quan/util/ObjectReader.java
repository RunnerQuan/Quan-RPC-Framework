package com.quan.util;

import com.quan.entity.RpcRequest;
import com.quan.entity.RpcResponse;
import com.quan.enumeration.PackageType;
import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import com.quan.serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 从输入流中读取字节并反序列化为对象
 * @author Quan
 */
public class ObjectReader {
    // 日志记录
    private static final Logger logger = LoggerFactory.getLogger(ObjectReader.class);
    // 魔数：用于验证协议包的合法性；选择0xCAFEBABE因为Java图标是个咖啡杯hhh~
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    // 从输入流中读取字节并反序列化为对象
    public static Object readObject(InputStream in) throws IOException {
        byte[] numberBytes = new byte[4];
        in.read(numberBytes);
        int magic = bytesToInt(numberBytes);
        if(magic != MAGIC_NUMBER) {
            logger.error("未知的协议包：{}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }

        in.read(numberBytes);
        int packageCode = bytesToInt(numberBytes);
        Class<?> packageClass;
        if(packageCode == PackageType.REQUEST_PACKAGE.getCode()) {
            packageClass = RpcRequest.class;
        } else if(packageCode == PackageType.RESPONSE_PACKAGE.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("未知的数据包：{}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }

        in.read(numberBytes);
        int serializerCode = bytesToInt(numberBytes);
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if(serializer == null) {
            logger.error("未知的反序列化器：{}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_DESERIALIZER);
        }

        in.read(numberBytes);
        int length = bytesToInt(numberBytes);
        byte[] bytes = new byte[length];
        in.read(bytes);
        return serializer.deserialize(bytes, packageClass);
    }

    // 将4个字节的byte数组转换为int类型
    public static int bytesToInt(byte[] src) {
        int value;
        // 大端序，例如字节数组{1, 2, 3, 4}，1是最低位，4是最高位
        // & 0xFF是为了将byte转换为int(Java中的字节是有符号的，我需要把它视作无符号整数)
        value = (src[0] & 0xFF)
                | ((src[1] & 0xFF) << 8)
                | ((src[2] & 0xFF) << 16)
                | ((src[3] & 0xFF) << 24);
        return value;
    }
}
