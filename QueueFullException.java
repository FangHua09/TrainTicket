package exception;

/**
 * 队列已满异常，用于模拟洪峰流量下的拒绝策略。
 * 当请求队列达到容量上限时抛出此异常。
 */
public class QueueFullException extends RuntimeException {
    
    /**
     * 构造函数，创建队列已满异常。
     * @param message 异常消息，包含请求ID等信息
     */
    public QueueFullException(String message) {
        super(message);
    }
}