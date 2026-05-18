package scheduler;

import entity.TicketRequest;

import java.util.List;

/**
 * 请求调度器接口，定义调度算法的标准行为。
 * 策略模式：所有调度器实现此接口。
 */
public interface RequestScheduler {

    /**
     * 对请求列表进行调度排序。
     * @param requests 待调度的请求列表
     * @return 排序后的请求列表
     */
    List<TicketRequest> schedule(List<TicketRequest> requests);

    /**
     * 获取调度器名称。
     * @return 调度器名称
     */
    String name();
}