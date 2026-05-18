package service;

import concurrent.IdempotentChecker;
import concurrent.StockManager;
import entity.Order;
import entity.TicketRequest;
import entity.Train;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务，负责下单、回滚和结果返回。
 * 核心流程：检查车次 -> 幂等检查 -> 扣库存 -> 生成订单
 */
public class OrderService {

    /** 库存管理器 */
    private final StockManager stockManager;
    /** 幂等检查器 */
    private final IdempotentChecker idempotentChecker;
    /** 订单ID生成器 */
    private final AtomicLong orderIdGenerator = new AtomicLong(1L);

    /**
     * 构造函数。
     * @param stockManager 库存管理器
     * @param idempotentChecker 幂等检查器
     */
    public OrderService(StockManager stockManager, IdempotentChecker idempotentChecker) {
        this.stockManager = stockManager;
        this.idempotentChecker = idempotentChecker;
    }

    /**
     * 处理抢票请求。
     * @param request 抢票请求
     * @param queueWaitMs 队列等待时间（毫秒）
     * @return 订单结果
     */
    public Order process(TicketRequest request, long queueWaitMs) {
        long begin = System.currentTimeMillis();  // 记录开始时间
        
        // 1. 检查车次是否存在
        Train train = stockManager.getTrain(request.getTrainNo());
        if (train == null) {
            return fail(request, queueWaitMs, begin, "车次不存在");
        }

        // 2. 幂等检查：防止重复下单
        String businessKey = request.businessKey();
        if (!idempotentChecker.checkAndMark(businessKey, request.getRequestId())) {
            return fail(request, queueWaitMs, begin, "重复下单");
        }

        boolean reserved = false;  // 库存预留标记
        try {
            // 3. 预留库存（扣减票数）
            reserved = stockManager.reserve(request.getTrainNo(), request.getRequestedCount());
            if (!reserved) {
                // 库存不足，回滚幂等标记
                idempotentChecker.rollback(businessKey);
                return fail(request, queueWaitMs, begin, "余票不足");
            }

            // 4. 生成成功订单
            long end = System.currentTimeMillis();
            return new Order(
                    nextOrderId(),
                    request.getRequestId(),
                    request.getUserId(),
                    train.getTrainNo(),
                    train.getDeparture(),
                    train.getDestination(),
                    train.getSeatClass(),
                    true,
                    "SUCCESS",
                    end,
                    queueWaitMs,
                    end - begin
            );
        } catch (RuntimeException ex) {
            // 异常处理：回滚库存和幂等标记
            if (reserved) {
                stockManager.rollback(request.getTrainNo(), request.getRequestedCount());
            }
            idempotentChecker.rollback(businessKey);
            return fail(request, queueWaitMs, begin, "异常回滚: " + ex.getMessage());
        }
    }

    /**
     * 生成失败订单。
     * @param request 抢票请求
     * @param queueWaitMs 队列等待时间
     * @param begin 开始时间
     * @param reason 失败原因
     * @return 失败订单
     */
    public Order fail(TicketRequest request, long queueWaitMs, long begin, String reason) {
        long end = System.currentTimeMillis();
        return new Order(
                nextOrderId(),
                request.getRequestId(),
                request.getUserId(),
                request.getTrainNo(),
                "",
                "",
                request.getSeatClass(),
                false,
                reason,
                end,
                queueWaitMs,
                end - begin
        );
    }

    /**
     * 生成下一个订单ID。
     * @return 订单ID
     */
    private String nextOrderId() {
        return "ORD-" + orderIdGenerator.getAndIncrement();
    }
}