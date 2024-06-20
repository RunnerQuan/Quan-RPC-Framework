package com.quan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloServiceImplementation implements HelloService {
    // 日志记录
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImplementation.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到：{}", object.getMessage());
        return "这是调用的返回值， id = " + object.getId();
    }
}
