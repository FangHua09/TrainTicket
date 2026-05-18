package concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置中心，统一管理核心线程数、最大线程数、队列容量和拒绝策略。
 */
public class TicketPool {

    /** ThreadPoolExecutor实例 */
    private final ThreadPoolExecutor executor;

    /**
     * 构造函数，创建线程池。
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param queueCapacity 队列容量
     */
    public TicketPool(int corePoolSize, int maximumPoolSize, int queueCapacity) {
        this.executor = new ThreadPoolExecutor(
                corePoolSize,  // 核心线程数
                maximumPoolSize,  // 最大线程数
                60L,  // 空闲线程存活时间（秒）
                TimeUnit.SECONDS,  // 时间单位
                new LinkedBlockingQueue<Runnable>(queueCapacity),  // 任务队列
                new NamedThreadFactory("ticket-pool"),  // 线程工厂
                new LoggingRejectedExecutionHandler());  // 拒绝策略
    }

    /**
     * 获取ThreadPoolExecutor实例。
     * @return ThreadPoolExecutor
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * 优雅关闭线程池（等待已有任务完成）。
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 强制关闭线程池（立即停止所有任务）。
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }

    /**
     * 命名线程工厂，为线程设置有意义的名称。
     */
    private static final class NamedThreadFactory implements ThreadFactory {
        /** 线程名称前缀 */
        private final String prefix;
        /** 线程序号生成器 */
        private final AtomicInteger index = new AtomicInteger(1);

        /**
         * 构造函数。
         * @param prefix 线程名称前缀
         */
        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        /**
         * 创建新线程。
         * @param runnable 线程执行的任务
         * @return 新创建的线程
         */
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + "-" + index.getAndIncrement());  // 设置线程名称
            thread.setDaemon(false);  // 设置为非守护线程
            return thread;
        }
    }

    /**
     * 日志拒绝策略，当线程池拒绝任务时抛出异常。
     */
    private static final class LoggingRejectedExecutionHandler implements RejectedExecutionHandler {
        /**
         * 处理被拒绝的任务。
         * @param runnable 被拒绝的任务
         * @param executor 线程池
         */
        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            throw new java.util.concurrent.RejectedExecutionException("线程池拒绝任务，当前队列已满");
        }
    }
}