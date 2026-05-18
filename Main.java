import scheduler.SchedulerType;
import service.TicketService;
import service.TrainService;
import service.UserService;
import stats.BenchmarkResult;
import stats.ReportGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 项目入口，执行三种调度算法对比压测并输出报告。
 */
public class Main {

    public static void main(String[] args) {
        // 默认请求规模列表：1000, 5000, 10000
        List<Integer> requestScales = new ArrayList<Integer>(Arrays.asList(1000, 5000, 10000));
        int userCount = 500;  // 默认用户数
        String reportFile = "ticket-sim-report.txt";  // 默认报告文件名

        // 解析命令行参数
        if (args.length > 0) {
            requestScales = parseScales(args[0], requestScales);  // 第1个参数：请求数（可逗号分隔多个）
        }
        if (args.length > 1) {
            userCount = parseInt(args[1], userCount);  // 第2个参数：用户数
        }
        if (args.length > 2) {
            reportFile = args[2];  // 第3个参数：报告文件名
        }

        // 初始化服务
        TrainService trainService = new TrainService();
        trainService.initializeDemoTrains();  // 初始化演示车次

        UserService userService = new UserService();
        TicketService ticketService = new TicketService(userService, trainService);

        // 运行压测
        List<BenchmarkResult> results = new ArrayList<BenchmarkResult>();
        for (Integer requestCount : requestScales) {
            // 对每种请求规模，测试三种调度算法
            results.add(ticketService.runBenchmark(SchedulerType.FIFO, userCount, requestCount));
            results.add(ticketService.runBenchmark(SchedulerType.PRIORITY, userCount, requestCount));
            results.add(ticketService.runBenchmark(SchedulerType.TIME_SLICE, userCount, requestCount));
        }

        // 生成并导出报告
        ReportGenerator reportGenerator = new ReportGenerator();
        String report = reportGenerator.generate(results);
        reportGenerator.export(report, reportFile);

        // 输出报告到控制台
        System.out.println(report);
        System.out.println("报告已导出到: " + reportFile);
    }

    /**
     * 解析整数参数，失败返回默认值。
     * @param value 字符串值
     * @param fallback 默认值
     * @return 解析后的整数
     */
    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    /**
     * 解析请求规模参数（支持逗号分隔多个值）。
     * @param value 字符串值
     * @param fallback 默认列表
     * @return 请求规模列表
     */
    private static List<Integer> parseScales(String value, List<Integer> fallback) {
        try {
            String[] parts = value.split(",");  // 逗号分隔
            Set<Integer> scales = new LinkedHashSet<Integer>();  // 使用Set去重，保持顺序
            for (String part : parts) {
                int parsed = Integer.parseInt(part.trim());
                if (parsed > 0) {
                    scales.add(parsed);
                }
            }
            if (scales.isEmpty()) {
                return fallback;  // 没有有效值，返回默认
            }
            return new ArrayList<Integer>(scales);
        } catch (NumberFormatException ex) {
            return fallback;  // 解析失败，返回默认
        }
    }
}