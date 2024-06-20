package com.quan.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NoArgsConstructor;

import java.util.concurrent.*;

@NoArgsConstructor
public class ThreadPoolFactory {
    /**
     * 线程池的参数
     */
    // 线程池的核心线程数量
    private static final int CORE_POOL_SIZE = 15;
    // 线程池的最大线程数量：包括核心线程和临时创建的线程
    private static final int MAXIMUM_POOL_SIZE = 100;
    // 空闲线程等待新任务的最长时间(1 min)：超过这个时间如果还没有新任务到达，这些线程将被终止
    private static final int TIME_TO_LIVE = 1;
    // 线程池的工作队列容量（阻塞队列用于存储待处理的任务）
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    // 用来指定线程池中工作线程的名称前缀
    public static ExecutorService createDefaultThreadPool(String threadName) {
        // 创建的线程默认不是守护线程
        return createDefaultThreadPool(threadName, false);
    }

    // 创建线程池
    public static ExecutorService createDefaultThreadPool(String threadName, Boolean daemon) {
        // 使用有界的队列，可以防止资源耗尽问题
        // 当队列满时，新提交的任务可能会等待执行或者被拒绝
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = createThreadFactory(threadName, daemon);
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, TIME_TO_LIVE, TimeUnit.MINUTES, workQueue, threadFactory);
    }

    // 创建线程工厂
    // 设置线程的名字和是否为守护线程：如果线程名不为空，则设置线程名，否则使用默认的线程工厂
    private static ThreadFactory createThreadFactory(String threadName, Boolean daemon) {
        if(threadName != null) {
            if(daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadName + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadName + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }
}
