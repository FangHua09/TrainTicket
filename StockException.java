package exception;

/**
 * 库存异常，避免超卖时抛出。
 * 当库存扣减失败或库存不足时抛出此异常。
 */
public class StockException extends RuntimeException {
    
    /**
     * 构造函数，创建库存异常。
     * @param message 异常消息，说明库存问题
     */
    public StockException(String message) {
        super(message);
    }
}