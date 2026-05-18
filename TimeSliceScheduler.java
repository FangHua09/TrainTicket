package scheduler;

import entity.TicketRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;

/**
 * 时间片调度器，按固定批次轮转处理请求，减少单批长时间独占资源。
 * 按用户分组，每个用户每次处理sliceSize个请求，实现公平调度。
 */
public class TimeSliceScheduler implements RequestScheduler {

    /** 每个时间片处理的请求数 */
    private final int sliceSize;

    /**
     * 构造函数。
     * @param sliceSize 每个时间片处理的请求数，默认1
     */
    public TimeSliceScheduler(int sliceSize) {
        this.sliceSize = sliceSize <= 0 ? 1 : sliceSize;  // 确保sliceSize至少为1
    }

    /**
     * 按时间片轮转调度请求。
     * 1. 先按用户ID分组
     * 2. 按轮次依次从每个用户组取sliceSize个请求
     * 3. 直到所有请求都被处理
     * @param requests 待调度的请求列表
     * @return 轮转排序后的请求列表
     */
    @Override
    public List<TicketRequest> schedule(List<TicketRequest> requests) {
        // 按用户ID分组，key为用户ID，value为该用户的请求队列
        Map<Long, Queue<TicketRequest>> buckets = new LinkedHashMap<Long, Queue<TicketRequest>>();
        for (TicketRequest request : requests) {
            Queue<TicketRequest> bucket = buckets.get(request.getUserId());
            if (bucket == null) {  // 用户组不存在，创建新队列
                bucket = new LinkedList<TicketRequest>();
                buckets.put(request.getUserId(), bucket);
            }
            bucket.offer(request);  // 将请求加入用户队列
        }

        // 按时间片轮转取出请求
        List<TicketRequest> ordered = new ArrayList<TicketRequest>(requests.size());
        boolean hasRemaining = true;  // 是否还有剩余请求
        while (hasRemaining) {
            hasRemaining = false;  // 假设本轮没有剩余
            for (Queue<TicketRequest> bucket : buckets.values()) {  // 遍历每个用户组
                int slice = 0;  // 当前时间片已处理的请求数
                // 每个用户组最多处理sliceSize个请求
                while (slice < sliceSize && !bucket.isEmpty()) {
                    ordered.add(bucket.poll());  // 取出请求
                    slice++;
                    hasRemaining = true;  // 还有剩余请求
                }
            }
        }
        return ordered;
    }

    /**
     * 获取调度器名称。
     * @return "TIME_SLICE"
     */
    @Override
    public String name() {
        return "TIME_SLICE";
    }
}