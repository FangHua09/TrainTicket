package entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 车次实体，使用原子变量保存剩余票数，避免并发写入冲突。
 * 每个车次对应一种座位类型的库存。
 */
public class Train {

    /** 车次编号 */
    private final String trainNo;
    /** 出发站 */
    private final String departure;
    /** 到达站 */
    private final String destination;
    /** 座位类型（如二等座、一等座、商务座） */
    private final String seatClass;
    /** 总票数 */
    private final int totalTickets;
    /** 剩余票数（原子变量，保证并发安全） */
    private final AtomicInteger remainingTickets;

    /**
     * 构造函数，创建车次实体。
     * @param trainNo 车次编号
     * @param departure 出发站
     * @param destination 到达站
     * @param seatClass 座位类型
     * @param totalTickets 总票数
     */
    public Train(String trainNo, String departure, String destination, String seatClass, int totalTickets) {
        this.trainNo = trainNo;
        this.departure = departure;
        this.destination = destination;
        this.seatClass = seatClass;
        this.totalTickets = totalTickets;
        this.remainingTickets = new AtomicInteger(totalTickets);  // 初始剩余票数等于总票数
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

    /** 获取总票数 */
    public int getTotalTickets() {
        return totalTickets;
    }

    /** 获取剩余票数的原子变量引用（用于库存扣减） */
    public AtomicInteger getRemainingTickets() {
        return remainingTickets;
    }

    /** 获取当前剩余票数 */
    public int currentRemaining() {
        return remainingTickets.get();
    }

    /** 返回车次的字符串表示 */
    @Override
    public String toString() {
        return "Train{" +
                "trainNo='" + trainNo + '\'' +
                ", departure='" + departure + '\'' +
                ", destination='" + destination + '\'' +
                ", seatClass='" + seatClass + '\'' +
                ", totalTickets=" + totalTickets +
                ", remainingTickets=" + remainingTickets.get() +
                '}';
    }
}