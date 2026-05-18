package concurrent;

import entity.TicketRequest;
import exception.QueueFullException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 对阻塞队列的简单封装，作为抢票请求的缓冲区。
 * 使用ArrayBlockingQueue实现，保证线程安全。
 */
public class TicketQueue {

    /** 底层阻塞队列，用于存储抢票请求 */
    private final BlockingQueue<TicketRequest> queue;

    /**
     * 构造函数，创建指定容量的队列。
     * @param capacity 队列容量
     */
    public TicketQueue(int capacity) {
        // 创建有界阻塞队列，使用公平策略
        this.queue = new ArrayBlockingQueue<TicketRequest>(capacity, true);
    }

    /**
     * 尝试将请求加入队列（非阻塞）。
     * @param request 抢票请求
     * @return 成功返回true，队列满返回false
     */
    public boolean offer(TicketRequest request) {
        return queue.offer(request);
    }

    /**
     * 将请求加入队列（阻塞直到成功或队列满抛出异常）。
     * @param request 抢票请求
     * @throws QueueFullException 队列已满时抛出
     */
    public void put(TicketRequest request) {
        if (!queue.offer(request)) {  // 尝试加入队列
            throw new QueueFullException("请求队列已满: " + request.getRequestId());
        }
    }

    /**
     * 获取并移除队列头部元素（非阻塞）。
     * @return 队列头部元素，队列为空返回null
     */
    public TicketRequest poll() {
        return queue.poll();
    }

    /**
     * 清空队列并返回所有元素。
     * @return 队列中的所有请求
     */
    public List<TicketRequest> drainAll() {
        List<TicketRequest> requests = new ArrayList<TicketRequest>(queue.size());
        queue.drainTo(requests);  // 将队列中所有元素转移到list中
        return requests;
    }

    /**
     * 将批量请求加入队列。
     * @param requests 请求集合
     */
    public void addAll(Collection<TicketRequest> requests) {
        for (TicketRequest request : requests) {
            put(request);  // 逐个加入队列
        }
    }

    /**
     * 获取队列当前大小。
     * @return 队列中的元素数量
     */
    public int size() {
        return queue.size();
    }
}