package exception;

/**
 * 排队超时异常。
 * 当请求在队列中等待时间过长时抛出此异常。
 */
public class TimeoutException extends RuntimeException {
    
    /**
     * 构造函数，创建超时异常。
     * @param message 异常消息，说明超时原因
     */
    public TimeoutException(String message) {
        super(message);
    }
}