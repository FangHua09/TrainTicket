package entity;

/**
 * 抢票请求实体，业务幂等判断基于同一用户 + 同一车次 + 同一席别。
 * 实现Comparable接口用于优先级排序。
 */
public class TicketRequest implements Comparable<TicketRequest> {

    /** 请求唯一标识符 */
    private final String requestId;
    /** 用户ID */
    private final long userId;
    /** 用户优先级 */
    private final int userPriority;
    /** 车次编号 */
    private final String trainNo;
    /** 座位类型 */
    private final String seatClass;
    /** 请求票数 */
    private final int requestedCount;
    /** 请求提交时间戳 */
    private final long submitTimestamp;
    /** 请求序号（用于同优先级同时间的排序） */
    private final long sequence;

    /**
     * 构造函数，创建抢票请求实体。
     * @param requestId 请求ID
     * @param userId 用户ID
     * @param userPriority 用户优先级
     * @param trainNo 车次编号
     * @param seatClass 座位类型
     * @param requestedCount 请求票数
     * @param submitTimestamp 提交时间戳
     * @param sequence 请求序号
     */
    public TicketRequest(String requestId, long userId, int userPriority, String trainNo, String seatClass,
                         int requestedCount, long submitTimestamp, long sequence) {
        this.requestId = requestId;
        this.userId = userId;
        this.userPriority = userPriority;
        this.trainNo = trainNo;
        this.seatClass = seatClass;
        this.requestedCount = requestedCount;
        this.submitTimestamp = submitTimestamp;
        this.sequence = sequence;
    }

    /** 获取请求ID */
    public String getRequestId() {
        return requestId;
    }

    /** 获取用户ID */
    public long getUserId() {
        return userId;
    }

    /** 获取用户优先级 */
    public int getUserPriority() {
        return userPriority;
    }

    /** 获取车次编号 */
    public String getTrainNo() {
        return trainNo;
    }

    /** 获取座位类型 */
    public String getSeatClass() {
        return seatClass;
    }

    /** 获取请求票数 */
    public int getRequestedCount() {
        return requestedCount;
    }

    /** 获取提交时间戳 */
    public long getSubmitTimestamp() {
        return submitTimestamp;
    }

    /** 获取请求序号 */
    public long getSequence() {
        return sequence;
    }

    /**
     * 生成业务键，用于幂等判断。
     * 业务键 = 用户ID|车次|座位类型|请求票数
     * @return 业务键字符串
     */
    public String businessKey() {
        return userId + "|" + trainNo + "|" + seatClass + "|" + requestedCount;
    }

    /**
     * 实现Comparable接口，用于优先级调度排序。
     * 排序规则：先按优先级降序，再按提交时间升序，最后按序号升序。
     * @param other 另一个请求对象
     * @return 比较结果（负数表示当前对象优先级更高）
     */
    @Override
    public int compareTo(TicketRequest other) {
        // 优先级比较：数值大的优先级高，所以用other.userPriority - this.userPriority
        int priorityCompare = Integer.compare(other.userPriority, this.userPriority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        // 提交时间比较：时间早的优先
        int timeCompare = Long.compare(this.submitTimestamp, other.submitTimestamp);
        if (timeCompare != 0) {
            return timeCompare;
        }
        // 序号比较：序号小的优先（保证稳定性）
        return Long.compare(this.sequence, other.sequence);
    }

    /** 返回请求的字符串表示 */
    @Override
    public String toString() {
        return "TicketRequest{" +
                "requestId='" + requestId + '\'' +
                ", userId=" + userId +
                ", userPriority=" + userPriority +
                ", trainNo='" + trainNo + '\'' +
                ", seatClass='" + seatClass + '\'' +
                ", requestedCount=" + requestedCount +
                ", submitTimestamp=" + submitTimestamp +
                ", sequence=" + sequence +
                '}';
    }
}