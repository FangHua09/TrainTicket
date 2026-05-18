package stats;

import entity.Order;

import java.util.List;

/**
 * 压测结果，包含调度器名称、请求规模、耗时、指标快照和订单列表。
 */
public class BenchmarkResult {

    /** 调度器名称 */
    private final String schedulerName;
    /** 请求数量 */
    private final int requestCount;
    /** 总耗时（毫秒） */
    private final long elapsedMs;
    /** 指标快照 */
    private final MetricsCollector.Snapshot metrics;
    /** 订单列表 */
    private final List<Order> orders;

    /**
     * 构造函数。
     * @param schedulerName 调度器名称
     * @param requestCount 请求数量
     * @param elapsedMs 总耗时
     * @param metrics 指标快照
     * @param orders 订单列表
     */
    public BenchmarkResult(String schedulerName, int requestCount, long elapsedMs,
                           MetricsCollector.Snapshot metrics, List<Order> orders) {
        this.schedulerName = schedulerName;
        this.requestCount = requestCount;
        this.elapsedMs = elapsedMs;
        this.metrics = metrics;
        this.orders = orders;
    }

    /** 获取调度器名称 */
    public String getSchedulerName() {
        return schedulerName;
    }

    /** 获取请求数量 */
    public int getRequestCount() {
        return requestCount;
    }

    /** 获取总耗时 */
    public long getElapsedMs() {
        return elapsedMs;
    }

    /** 获取指标快照 */
    public MetricsCollector.Snapshot getMetrics() {
        return metrics;
    }

    /** 获取订单列表 */
    public List<Order> getOrders() {
        return orders;
    }
}