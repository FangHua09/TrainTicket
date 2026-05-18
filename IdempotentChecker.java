package concurrent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 幂等控制，防止同一业务键重复下单。
 * 使用ConcurrentHashMap的putIfAbsent实现原子性的幂等检查。
 */
public class IdempotentChecker {

    /** 存储已处理的业务键，key为业务键，value为请求ID */
    private final ConcurrentHashMap<String, String> processedKeys = new ConcurrentHashMap<String, String>();

    /**
     * 检查并标记业务键。
     * 如果业务键未被处理，则标记为已处理；否则返回false表示重复请求。
     * @param businessKey 业务键（用户ID+车次+座位类型+票数）
     * @param requestId 请求ID
     * @return 首次处理返回true，重复请求返回false
     */
    public boolean checkAndMark(String businessKey, String requestId) {
        // putIfAbsent：如果key不存在则插入并返回null，否则返回已存在的value
        return processedKeys.putIfAbsent(businessKey, requestId) == null;
    }

    /**
     * 回滚业务键标记（订单失败时调用）。
     * @param businessKey 业务键
     */
    public void rollback(String businessKey) {
        processedKeys.remove(businessKey);  // 移除业务键标记
    }

    /**
     * 检查业务键是否已被处理。
     * @param businessKey 业务键
     * @return 已处理返回true，未处理返回false
     */
    public boolean isProcessed(String businessKey) {
        return processedKeys.containsKey(businessKey);
    }
}