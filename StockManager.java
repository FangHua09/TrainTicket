package concurrent;

import entity.Train;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 库存管理器，使用 ConcurrentHashMap 存储车次，并通过 ReentrantLock + 原子变量避免超卖。
 * 提供库存预留、回滚和查询功能，保证线程安全。
 */
public class StockManager {

    /** 存储车次信息，key为车次编号 */
    private final ConcurrentHashMap<String, Train> trains = new ConcurrentHashMap<String, Train>();
    /** 存储每个车次对应的锁，key为车次编号，保证同一车次的操作互斥 */
    private final ConcurrentHashMap<String, ReentrantLock> trainLocks = new ConcurrentHashMap<String, ReentrantLock>();

    /**
     * 注册车次到库存管理器。
     * @param train 车次实体
     */
    public void registerTrain(Train train) {
        trains.put(train.getTrainNo(), train);  // 存储车次信息
        // 为该车次创建锁（公平锁），保证先到先得
        trainLocks.putIfAbsent(train.getTrainNo(), new ReentrantLock(true));
    }

    /**
     * 根据车次编号获取车次信息。
     * @param trainNo 车次编号
     * @return 车次实体，不存在返回null
     */
    public Train getTrain(String trainNo) {
        return trains.get(trainNo);
    }

    /**
     * 预留库存（扣减票数）。
     * 使用ReentrantLock + CAS双重保护，确保不会超卖。
     * @param trainNo 车次编号
     * @param count 需要预留的票数
     * @return 预留成功返回true，失败返回false
     */
    public boolean reserve(String trainNo, int count) {
        Train train = trains.get(trainNo);  // 获取车次信息
        if (train == null) {  // 车次不存在
            return false;
        }
        ReentrantLock lock = trainLocks.get(trainNo);  // 获取该车次的锁
        lock.lock();  // 获取锁，保证同一车次的操作串行化
        try {
            AtomicInteger remaining = train.getRemainingTickets();  // 获取剩余票数的原子变量
            while (true) {  // 自旋重试，处理CAS失败的情况
                int current = remaining.get();  // 获取当前剩余票数
                if (current < count) {  // 库存不足，直接返回失败
                    return false;
                }
                // CAS操作：如果当前值等于current，则更新为current - count
                if (remaining.compareAndSet(current, current - count)) {
                    return true;  // CAS成功，预留成功
                }
                // CAS失败，说明有其他线程修改了库存，重新循环重试
            }
        } finally {
            lock.unlock();  // 释放锁
        }
    }

    /**
     * 回滚库存（恢复票数）。
     * @param trainNo 车次编号
     * @param count 需要恢复的票数
     */
    public void rollback(String trainNo, int count) {
        Train train = trains.get(trainNo);  // 获取车次信息
        if (train == null) {  // 车次不存在，直接返回
            return;
        }
        ReentrantLock lock = trainLocks.get(trainNo);  // 获取锁
        lock.lock();  // 获取锁
        try {
            // 增加剩余票数
            train.getRemainingTickets().addAndGet(count);
        } finally {
            lock.unlock();  // 释放锁
        }
    }

    /**
     * 获取所有车次的快照（深拷贝）。
     * @return 包含所有车次的ConcurrentHashMap
     */
    public ConcurrentHashMap<String, Train> snapshot() {
        return new ConcurrentHashMap<String, Train>(trains);
    }

    /**
     * 清空所有车次和锁。
     */
    public void clear() {
        trains.clear();
        trainLocks.clear();
    }
}