package com.quan.registry;

import com.quan.enumeration.RpcError;
import com.quan.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务注册表的默认实现类
 * 用于注册服务和发现服务
 * @author Quan
 */
public class QuanServiceRegistry implements ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(QuanServiceRegistry.class);

    // 注册中心的服务列表
    private static final ConcurrentHashMap<String, InetSocketAddress> serviceMap = new ConcurrentHashMap<>();

    // 服务的最后心跳时间
    private static final ConcurrentHashMap<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();

    // 服务失活阈值，单位为毫秒
    private static final long SERVICE_TIMEOUT = 15 * 1000;

    public QuanServiceRegistry() {
        // 启动定时任务，定期检查服务状态
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkServices, 0, 10, TimeUnit.SECONDS);
    }

    // 注册服务
    @Override
    public synchronized void register(String serviceName, InetSocketAddress inetSocketAddress) {
        if(serviceMap.containsKey(serviceName)) {
            logger.warn("已经注册过该服务：{}", serviceName);
            return;
        }
        serviceMap.put(serviceName, inetSocketAddress);
        logger.info("服务：{} 注册进服务注册中心", serviceName);
    }

    // 发现服务
    @Override
    public InetSocketAddress discoverService(String serviceName) {
        InetSocketAddress address = serviceMap.get(serviceName);
        if(address == null) {
            logger.error("找不到对应的服务：{}", serviceName);
            throw new RpcException(RpcError.SERVICE_NO_FOUND);
        }
        logger.info("发现服务：{} 的地址为：{}", serviceName, address);
        return address;
    }

    // 处理心跳包
    public void heartbeat(String serviceName, InetSocketAddress ineSocketAddress) {
        if (serviceMap.containsKey(serviceName)) {
            lastHeartbeatMap.put(serviceName, System.currentTimeMillis());
//            logger.info("收到服务：{} 的心跳包", serviceName);
        } else {
            register(serviceName, ineSocketAddress);
//            logger.info("未注册服务：{}，进行注册", serviceName);
        }
    }

    private void checkServices() {
        long currentTime = System.currentTimeMillis();
        for(String serviceName : lastHeartbeatMap.keySet()) {
            long lastHeartbeatTime = lastHeartbeatMap.get(serviceName);
            if(currentTime - lastHeartbeatTime > SERVICE_TIMEOUT) {
                serviceMap.remove(serviceName);
                lastHeartbeatMap.remove(serviceName);
                logger.info("服务：{}已失活，被移除服务注册中心", serviceName);
            }
        }
    }

    @Override
    // 获取服务列表
    public ConcurrentHashMap<String, InetSocketAddress> getServiceMap() {
        return serviceMap;
    }
}
