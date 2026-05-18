package service;

import concurrent.IdempotentChecker;
import concurrent.TicketPool;
import concurrent.TicketQueue;
import entity.Order;
import entity.TicketRequest;
import scheduler.RequestScheduler;
import scheduler.SchedulerFactory;
import scheduler.SchedulerType;
import stats.BenchmarkResult;
import stats.MetricsCollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 抢票总入口，串联队列、调度、线程池、库存和指标统计。
 */
public class TicketService {

    /** 用户服务 */
    private final UserService userService;
    /** 车次服务 */
    private final TrainService trainService;
    /** 调度器工厂 */
    private final SchedulerFactory schedulerFactory = new SchedulerFactory();

    /**
     * 构造函数。
     * @param userService 用户服务
     * @param trainService 车次服务
     */
    public TicketService(UserService userService, TrainService trainService) {
        this.userService = userService;
        this.trainService = trainService;
    }

    /**
     * 运行压测基准测试。
     * @param schedulerType 调度器类型
     * @param userCount 用户数量
     * @param requestCount 请求数量
     * @return 压测结果
     */
    public BenchmarkResult runBenchmark(SchedulerType schedulerType, int userCount, int requestCount) {
        // 1. 初始化车次数据
        trainService.initializeDemoTrains();
        
        // 2. 批量生成用户
        List<entity.User> users = userService.batchLogin(userCount);
        
        // 3. 生成抢票请求
        List<TicketRequest> requests = userService.generateRequests(
                requestCount, 
                users, 
                trainService.trainNos(), 
                trainService.seatClasses()
        );

        // 4. 创建请求队列并加入所有请求（容量根据实际请求数设置）
        TicketQueue ticketQueue = new TicketQueue(Math.max(requests.size(), 1));
        ticketQueue.addAll(requests);
        List<TicketRequest> waitingRequests = ticketQueue.drainAll();  // 取出所有请求

        // 5. 使用调度器重排请求顺序
        RequestScheduler scheduler = schedulerFactory.create(schedulerType);
        List<TicketRequest> orderedRequests = scheduler.schedule(waitingRequests);

        // 6. 创建线程池
        TicketPool ticketPool = new TicketPool(
                Math.max(4, Runtime.getRuntime().availableProcessors()),  // 核心线程数
                Math.max(8, Runtime.getRuntime().availableProcessors() * 2),  // 最大线程数
                Math.max(64, requestCount / 2)  // 队列容量
        );

        // 7. 初始化指标收集器和订单服务
        MetricsCollector metricsCollector = new MetricsCollector();
        OrderService orderService = new OrderService(
                trainService.getStockManager(), 
                new IdempotentChecker()
        );
        
        // 8. 准备并发执行
        List<Order> completedOrders = Collections.synchronizedList(new ArrayList<Order>(orderedRequests.size()));
        ThreadPoolExecutor executor = ticketPool.getExecutor();
        List<Future<Order>> futures = new ArrayList<Future<Order>>(orderedRequests.size());
        AtomicInteger inflightCounter = new AtomicInteger(0);  // 正在处理的请求计数器
        int maxInflight = Math.max(32, Runtime.getRuntime().availableProcessors() * 8);  // 限流阈值

        // 9. 开始计时并提交任务
        long begin = System.currentTimeMillis();
        for (TicketRequest request : orderedRequests) {
            final long queueWaitMs = Math.max(0L, System.currentTimeMillis() - request.getSubmitTimestamp());
            
            try {
                // 限流检查：超过阈值则直接拒绝
                if (inflightCounter.incrementAndGet() > maxInflight) {
                    inflightCounter.decrementAndGet();
                    Order rejected = orderService.fail(request, queueWaitMs, System.currentTimeMillis(), "被限流");
                    completedOrders.add(rejected);
                    metricsCollector.record(rejected);
                    continue;
                }

                // 提交异步任务
                Future<Order> future = executor.submit(new java.util.concurrent.Callable<Order>() {
                    @Override
                    public Order call() {
                        try {
                            return orderService.process(request, queueWaitMs);
                        } finally {
                            inflightCounter.decrementAndGet();  // 完成后减少计数器
                        }
                    }
                });
                futures.add(future);
            } catch (RuntimeException ex) {
                inflightCounter.decrementAndGet();
                Order rejected = orderService.fail(request, queueWaitMs, System.currentTimeMillis(), "线程池拒绝: " + ex.getMessage());
                completedOrders.add(rejected);
                metricsCollector.record(rejected);
            }
        }

        // 10. 等待所有任务完成
        for (Future<Order> future : futures) {
            try {
                Order order = future.get();  // 阻塞等待任务完成
                completedOrders.add(order);
                metricsCollector.record(order);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // 恢复中断状态
                Order interrupted = new Order(
                        "ORD-INTERRUPTED",
                        "INTERRUPTED",
                        -1L,
                        "",
                        "",
                        "",
                        "",
                        false,
                        "线程中断",
                        System.currentTimeMillis(),
                        0L,
                        0L);
                completedOrders.add(interrupted);
                metricsCollector.record(interrupted);
            } catch (ExecutionException e) {
                Order failed = new Order(
                        "ORD-FAILED",
                        "FAILED",
                        -1L,
                        "",
                        "",
                        "",
                        "",
                        false,
                        "执行异常: " + e.getCause().getMessage(),
                        System.currentTimeMillis(),
                        0L,
                        0L);
                completedOrders.add(failed);
                metricsCollector.record(failed);
            }
        }

        // 11. 关闭线程池并返回结果
        ticketPool.shutdown();
        long elapsedMs = Math.max(1L, System.currentTimeMillis() - begin);
        return new BenchmarkResult(scheduler.name(), requestCount, elapsedMs, metricsCollector.snapshot(), completedOrders);
    }
}