package stats;

import entity.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标采集器，统计 QPS、成功率、平均响应时间和 P95 时延。
 */
public class MetricsCollector {

    /** 采集开始时间 */
    private final long startTimeMs = System.currentTimeMillis();
    /** 总请求数 */
    private final AtomicLong totalCount = new AtomicLong();
    /** 成功请求数 */
    private final AtomicLong successCount = new AtomicLong();
    /** 失败请求数 */
    private final AtomicLong failureCount = new AtomicLong();
    /** 总时延（纳秒） */
    private final AtomicLong totalLatencyNs = new AtomicLong();
    /** 存储所有时延（毫秒）用于计算P95 */
    private final ConcurrentLinkedQueue<Long> latenciesMs = new ConcurrentLinkedQueue<Long>();

    /**
     * 记录订单结果，更新统计指标。
     * @param order 订单结果
     */
    public void record(Order order) {
        totalCount.incrementAndGet();  // 总请求数+1
        
        if (order.isSuccess()) {
            successCount.incrementAndGet();  // 成功数+1
        } else {
            failureCount.incrementAndGet();  // 失败数+1
        }
        
        // 计算总时延（队列等待时间 + 处理时间）
        long latency = Math.max(0L, order.getProcessingTimeMs() + order.getQueueWaitMs());
        totalLatencyNs.addAndGet(latency * 1_000_000L);  // 转换为纳秒
        latenciesMs.add(latency);  // 记录时延用于P95计算
    }

    /**
     * 获取当前指标快照。
     * @return 指标快照
     */
    public Snapshot snapshot() {
        long total = totalCount.get();
        long success = successCount.get();
        long failure = failureCount.get();
        long elapsed = Math.max(1L, System.currentTimeMillis() - startTimeMs);  // 总耗时
        
        // 计算QPS = 总请求数 / 总耗时（秒）
        double qps = total * 1000.0d / elapsed;
        
        // 计算成功率 = 成功数 / 总数 * 100%
        double successRate = total == 0 ? 0.0d : success * 100.0d / total;
        
        // 计算平均时延 = 总时延 / 总数（毫秒）
        double avgLatency = total == 0 ? 0.0d : totalLatencyNs.get() / 1_000_000.0d / total;
        
        // 计算P95时延
        double p95 = calculateP95();
        
        return new Snapshot(total, success, failure, elapsed, qps, successRate, avgLatency, p95);
    }

    /**
     * 计算P95时延（95%的请求在此时间内完成）。
     * @return P95时延（毫秒）
     */
    private double calculateP95() {
        List<Long> values = new ArrayList<Long>(latenciesMs);
        if (values.isEmpty()) {
            return 0.0d;
        }
        
        Collections.sort(values);  // 升序排序
        
        // P95位置 = 向上取整(总数 * 0.95) - 1
        int index = (int) Math.ceil(values.size() * 0.95d) - 1;
        
        // 边界保护
        index = Math.max(0, Math.min(index, values.size() - 1));
        
        return values.get(index);
    }

    /**
     * 指标快照，包含所有统计数据。
     */
    public static class Snapshot {
        /** 总请求数 */
        private final long totalCount;
        /** 成功请求数 */
        private final long successCount;
        /** 失败请求数 */
        private final long failureCount;
        /** 总耗时（毫秒） */
        private final long elapsedMs;
        /** QPS（每秒处理请求数） */
        private final double qps;
        /** 成功率（%） */
        private final double successRate;
        /** 平均时延（毫秒） */
        private final double averageLatencyMs;
        /** P95时延（毫秒） */
        private final double p95LatencyMs;

        /**
         * 构造函数。
         * @param totalCount 总请求数
         * @param successCount 成功请求数
         * @param failureCount 失败请求数
         * @param elapsedMs 总耗时
         * @param qps QPS
         * @param successRate 成功率
         * @param averageLatencyMs 平均时延
         * @param p95LatencyMs P95时延
         */
        public Snapshot(long totalCount, long successCount, long failureCount, long elapsedMs,
                        double qps, double successRate, double averageLatencyMs, double p95LatencyMs) {
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.elapsedMs = elapsedMs;
            this.qps = qps;
            this.successRate = successRate;
            this.averageLatencyMs = averageLatencyMs;
            this.p95LatencyMs = p95LatencyMs;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getSuccessCount() {
            return successCount;
        }

        public long getFailureCount() {
            return failureCount;
        }

        public long getElapsedMs() {
            return elapsedMs;
        }

        public double getQps() {
            return qps;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAverageLatencyMs() {
            return averageLatencyMs;
        }

        public double getP95LatencyMs() {
            return p95LatencyMs;
        }
    }
}