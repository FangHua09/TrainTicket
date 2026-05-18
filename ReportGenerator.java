package stats;

import entity.Order;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 报告生成器，将压测结果导出为文本报告。
 */
public class ReportGenerator {

    /**
     * 生成压测报告。
     * @param results 压测结果列表
     * @return 报告内容
     */
    public String generate(List<BenchmarkResult> results) {
        StringBuilder sb = new StringBuilder();
        
        // 报告标题
        sb.append("===== 高并发高铁抢票模拟系统压测报告 =====\n\n");
        
        // 按请求规模分组输出
        int lastScale = -1;
        for (BenchmarkResult result : results) {
            int scale = result.getRequestCount();
            
            // 新的请求规模，输出分隔线和表头
            if (scale != lastScale) {
                sb.append("--- 并发规模: ").append(scale).append(" 请求 ---\n");
                sb.append(String.format("%-12s\t%-8s\t%-10s\t%-15s\t%-8s\t%-12s\n", 
                        "算法", "成功率", "QPS", "平均时延(ms)", "P95(ms)", "总耗时(ms)"));
                lastScale = scale;
            }
            
            // 输出数据行
            MetricsCollector.Snapshot metrics = result.getMetrics();
            sb.append(String.format("%-12s\t%.2f%%\t%.2f\t%.2f\t%.2f\t%d\n",
                    result.getSchedulerName(),
                    metrics.getSuccessRate(),
                    metrics.getQps(),
                    metrics.getAverageLatencyMs(),
                    metrics.getP95LatencyMs(),
                    result.getElapsedMs()));
        }
        
        // 添加优化对比
        sb.append("\n优化前后对比(基线 FIFO -> 其他算法)：\n");
        for (BenchmarkResult result : results) {
            if ("FIFO".equals(result.getSchedulerName())) {
                continue;  // FIFO作为基线，跳过
            }
            
            // 查找相同规模的FIFO结果
            BenchmarkResult baseline = findBaseline(results, result.getRequestCount());
            if (baseline != null) {
                MetricsCollector.Snapshot baseMetrics = baseline.getMetrics();
                MetricsCollector.Snapshot metrics = result.getMetrics();
                
                // 计算QPS提升百分比
                double qpsImprovement = baseMetrics.getQps() == 0 ? 0 : 
                        (metrics.getQps() - baseMetrics.getQps()) / baseMetrics.getQps() * 100;
                
                // 计算平均时延变化
                double latencyChange = metrics.getAverageLatencyMs() - baseMetrics.getAverageLatencyMs();
                
                sb.append(String.format("%s 相对 FIFO 的 QPS 提升 %.2f%%，平均时延变化 %.2f ms\n",
                        result.getSchedulerName(), qpsImprovement, latencyChange));
            }
        }
        
        // 添加订单样例
        sb.append("\n===== 订单样例 =====\n");
        for (BenchmarkResult result : results) {
            sb.append("[").append(result.getSchedulerName()).append("]\n");
            List<Order> orders = result.getOrders();
            int sampleCount = Math.min(5, orders.size());
            for (int i = 0; i < sampleCount; i++) {
                sb.append(orders.get(i).toString()).append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * 查找相同请求规模的FIFO结果作为基线。
     * @param results 所有压测结果
     * @param requestCount 请求规模
     * @return FIFO结果
     */
    private BenchmarkResult findBaseline(List<BenchmarkResult> results, int requestCount) {
        for (BenchmarkResult result : results) {
            if ("FIFO".equals(result.getSchedulerName()) && result.getRequestCount() == requestCount) {
                return result;
            }
        }
        return null;
    }

    /**
     * 导出报告到文件。
     * @param report 报告内容
     * @param filename 文件名
     */
    public void export(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
        } catch (IOException e) {
            System.err.println("导出报告失败: " + e.getMessage());
        }
    }
}