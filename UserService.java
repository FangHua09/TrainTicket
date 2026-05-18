package service;

import entity.TicketRequest;
import entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户服务，负责用户登录、请求生成等功能。
 */
public class UserService {

    /** 用户ID生成器 */
    private final AtomicLong userIdGenerator = new AtomicLong(100000L);
    /** 用户名前缀 */
    private static final String USER_NAME_PREFIX = "user_";
    /** 随机数生成器 */
    private final Random random = new Random();

    /**
     * 模拟用户登录，生成唯一用户ID。
     * @return 登录后的用户实体
     */
    public User login() {
        long userId = userIdGenerator.getAndIncrement();  // 生成唯一用户ID
        String userName = USER_NAME_PREFIX + userId;  // 生成用户名
        int priority = random.nextInt(10) + 1;  // 随机生成优先级（1-10）
        return new User(userId, userName, priority, System.currentTimeMillis());
    }

    /**
     * 批量登录用户。
     * @param count 用户数量
     * @return 用户列表
     */
    public List<User> batchLogin(int count) {
        List<User> users = new ArrayList<User>(count);
        for (int i = 0; i < count; i++) {
            users.add(login());  // 逐个登录
        }
        return users;
    }

    /**
     * 生成抢票请求。
     * @param requestCount 请求数量
     * @param users 用户列表
     * @param trainNos 车次列表
     * @param seatClasses 座位类型列表
     * @return 请求列表
     */
    public List<TicketRequest> generateRequests(int requestCount, List<User> users, 
                                                List<String> trainNos, List<String> seatClasses) {
        List<TicketRequest> requests = new ArrayList<TicketRequest>(requestCount);
        AtomicLong sequenceGenerator = new AtomicLong(1L);  // 请求序号生成器
        
        for (int i = 0; i < requestCount; i++) {
            // 随机选择用户
            User user = users.get(random.nextInt(users.size()));
            // 随机选择车次
            String trainNo = trainNos.get(random.nextInt(trainNos.size()));
            // 随机选择座位类型
            String seatClass = seatClasses.get(random.nextInt(seatClasses.size()));
            // 生成请求ID
            String requestId = "REQ-" + (i + 1);
            // 生成请求
            TicketRequest request = new TicketRequest(
                    requestId,
                    user.getUserId(),
                    user.getPriority(),
                    trainNo,
                    seatClass,
                    1,  // 请求1张票
                    System.currentTimeMillis(),
                    sequenceGenerator.getAndIncrement()
            );
            requests.add(request);
            
            // 模拟重复请求（用于测试幂等性）
            if (i % 100 == 0 && i > 0) {
                // 每隔100个请求，复制一个重复请求（相同业务键）
                TicketRequest duplicate = new TicketRequest(
                        "REQ-DUP-" + (i + 1),
                        user.getUserId(),
                        user.getPriority(),
                        trainNo,
                        seatClass,
                        1,
                        System.currentTimeMillis(),
                        sequenceGenerator.getAndIncrement()
                );
                requests.add(duplicate);
            }
        }
        return requests;
    }

    /**
     * 获取已登录用户的快照。
     * @return 用户映射表（key为用户ID）
     */
    public ConcurrentHashMap<Long, User> snapshotUsers() {
        return new ConcurrentHashMap<Long, User>();
    }
}