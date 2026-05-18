package entity;

/**
 * 订单结果实体，同时承载成功、失败与失败原因。
 * 记录订单的完整信息，包括等待时间和处理时间。
 */
public class Order {

    /** 订单唯一标识符 */
    private final String orderId;
    /** 关联的请求ID */
    private final String requestId;
    /** 用户ID */
    private final long userId;
    /** 车次编号 */
    private final String trainNo;
    /** 出发站 */
    private final String departure;
    /** 到达站 */
    private final String destination;
    /** 座位类型 */
    private final String seatClass;
    /** 是否成功 */
    private final boolean success;
    /** 结果原因（成功为SUCCESS，失败为具体原因） */
    private final String reason;
    /** 订单创建时间戳 */
    private final long timestamp;
    /** 队列等待时间（毫秒） */
    private final long queueWaitMs;
    /** 处理时间（毫秒） */
    private final long processingTimeMs;

    /**
     * 构造函数，创建订单实体。
     * @param orderId 订单ID
     * @param requestId 请求ID
     * @param userId 用户ID
     * @param trainNo 车次编号
     * @param departure 出发站
     * @param destination 到达站
     * @param seatClass 座位类型
     * @param success 是否成功
     * @param reason 结果原因
     * @param timestamp 时间戳
     * @param queueWaitMs 队列等待时间
     * @param processingTimeMs 处理时间
     */
    public Order(String orderId, String requestId, long userId, String trainNo, String departure, String destination,
                 String seatClass, boolean success, String reason, long timestamp, long queueWaitMs, long processingTimeMs) {
        this.orderId = orderId;
        this.requestId = requestId;
        this.userId = userId;
        this.trainNo = trainNo;
        this.departure = departure;
        this.destination = destination;
        this.seatClass = seatClass;
        this.success = success;
        this.reason = reason;
        this.timestamp = timestamp;
        this.queueWaitMs = queueWaitMs;
        this.processingTimeMs = processingTimeMs;
    }

    /** 获取订单ID */
    public String getOrderId() {
        return orderId;
    }

    /** 获取请求ID */
    public String getRequestId() {
        return requestId;
    }

    /** 获取用户ID */
    public long getUserId() {
        return userId;
    }

    /** 获取车次编号 */
    public String getTrainNo() {
        return trainNo;
    }

    /** 获取出发站 */
    public String getDeparture() {
        return departure;
    }

    /** 获取到达站 */
    public String getDestination() {
        return destination;
    }

    /** 获取座位类型 */
    public String getSeatClass() {
        return seatClass;
    }

    /** 判断订单是否成功 */
    public boolean isSuccess() {
        return success;
    }

    /** 获取结果原因 */
    public String getReason() {
        return reason;
    }

    /** 获取时间戳 */
    public long getTimestamp() {
        return timestamp;
    }

    /** 获取队列等待时间 */
    public long getQueueWaitMs() {
        return queueWaitMs;
    }

    /** 获取处理时间 */
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    /** 返回订单的字符串表示 */
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", userId=" + userId +
                ", trainNo='" + trainNo + '\'' +
                ", seatClass='" + seatClass + '\'' +
                ", success=" + success +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                ", queueWaitMs=" + queueWaitMs +
                ", processingTimeMs=" + processingTimeMs +
                '}';
    }
}