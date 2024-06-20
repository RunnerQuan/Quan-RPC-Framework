package com.quan.util;

import com.quan.entity.RpcRequest;
import com.quan.enumeration.PackageType;
import com.quan.serializer.CommonSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 从输出流中读取字节并序列化为对象
 * @author Quan
 */
public class ObjectWriter {
    // 魔数：用于验证协议包的合法性
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    // 从输出流中读取字节并序列化为对象
    public static void writeObject(OutputStream outputStream, Object object, CommonSerializer serializer) throws IOException {
        outputStream.write(intToBytes(MAGIC_NUMBER));
        if(object instanceof RpcRequest) {
            outputStream.write(intToBytes(PackageType.REQUEST_PACKAGE.getCode()));
        } else {
            outputStream.write(intToBytes(PackageType.RESPONSE_PACKAGE.getCode()));
        }
        outputStream.write(intToBytes(serializer.getCode()));
        byte[] bytes = serializer.serialize(object);
        outputStream.write(intToBytes(bytes.length));
        outputStream.write(bytes);
        outputStream.flush();
    }

    // 将int类型转换为4字节的byte数组
    private static byte[] intToBytes(int value) {
        byte[] result = new byte[4];
        result[0] = (byte) (value & 0xFF);
        result[1] = (byte) ((value >> 8) & 0xFF);
        result[2] = (byte) ((value >> 16) & 0xFF);
        result[3] = (byte) ((value >> 24) & 0xFF);
        return result;
    }
}
