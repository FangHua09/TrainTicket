package scheduler;

import entity.TicketRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 优先级调度器，优先处理高优先级用户请求，再按提交时间排序。
 * 利用TicketRequest实现的Comparable接口进行排序。
 */
public class PriorityScheduler implements RequestScheduler {

    /**
     * 按优先级排序，高优先级优先。
     * @param requests 待调度的请求列表
     * @return 按优先级降序、提交时间升序排列的请求列表
     */
    @Override
    public List<TicketRequest> schedule(List<TicketRequest> requests) {
        List<TicketRequest> ordered = new ArrayList<TicketRequest>(requests);
        // 使用TicketRequest的compareTo方法排序：优先级高的优先，同优先级按时间
        Collections.sort(ordered);
        return ordered;
    }

    /**
     * 获取调度器名称。
     * @return "PRIORITY"
     */
    @Override
    public String name() {
        return "PRIORITY";
    }
}