package scheduler;

/**
 * 调度器工厂，根据类型创建对应的调度器实例。
 * 工厂模式：封装调度器的创建逻辑。
 */
public class SchedulerFactory {

    /**
     * 根据调度器类型创建调度器实例。
     * @param type 调度器类型
     * @return 对应的调度器实例
     */
    public RequestScheduler create(SchedulerType type) {
        switch (type) {
            case FIFO:  // FIFO调度器
                return new FIFOScheduler();
            case PRIORITY:  // 优先级调度器
                return new PriorityScheduler();
            case TIME_SLICE:  // 时间片调度器
                return new TimeSliceScheduler(3);  // 每个时间片处理3个请求
            default:  // 默认使用FIFO
                return new FIFOScheduler();
        }
    }
}