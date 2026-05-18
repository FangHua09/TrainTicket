package scheduler;

/**
 * 调度器类型枚举，定义支持的调度算法。
 */
public enum SchedulerType {

    /** FIFO调度（先进先出） */
    FIFO,
    
    /** 优先级调度 */
    PRIORITY,
    
    /** 时间片轮转调度 */
    TIME_SLICE;

    
}