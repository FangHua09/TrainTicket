package scheduler;

import entity.TicketRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FIFO调度器，完全按请求到达顺序处理。
 * 最简单的调度策略，保证公平性。
 */
public class FIFOScheduler implements RequestScheduler {

    /**
     * 按提交时间排序（FIFO）。
     * @param requests 待调度的请求列表
     * @return 按提交时间升序排列的请求列表
     */
    @Override
    public List<TicketRequest> schedule(List<TicketRequest> requests) {
        List<TicketRequest> ordered = new ArrayList<TicketRequest>(requests);
        // 按提交时间排序，时间早的优先
        Collections.sort(ordered, (a, b) -> Long.compare(a.getSubmitTimestamp(), b.getSubmitTimestamp()));
        return ordered;
    }

    /**
     * 获取调度器名称。
     * @return "FIFO"
     */
    @Override
    public String name() {
        return "FIFO";
    }
}