package com.quan;


import java.util.logging.Logger;

public class HelloServiceImplementation implements HelloService {
    // 用于记录与 com.quan.HelloServiceImplementation 相关的日志
    private static final Logger logger = Logger.getLogger(HelloServiceImplementation.class.getName());

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到：" + object.getMessage());
        return "这是调用的返回值， id=" + object.getId();
    }
}
