package entity;

import java.util.Objects;

/**
 * 用户实体，保存登录后的唯一用户标识和优先级信息。
 * 优先级决定了抢票请求的处理顺序，优先级越高越优先处理。
 */
public class User {

    /** 用户唯一标识符 */
    private final long userId;
    /** 用户名称 */
    private final String userName;
    /** 用户优先级，数值越大优先级越高 */
    private final int priority;
    /** 用户登录时间戳 */
    private final long loginTimestamp;

    /**
     * 构造函数，创建用户实体。
     * @param userId 用户ID
     * @param userName 用户名称
     * @param priority 用户优先级
     * @param loginTimestamp 登录时间戳
     */
    public User(long userId, String userName, int priority, long loginTimestamp) {
        this.userId = userId;
        this.userName = userName;
        this.priority = priority;
        this.loginTimestamp = loginTimestamp;
    }

    /** 获取用户ID */
    public long getUserId() {
        return userId;
    }

    /** 获取用户名称 */
    public String getUserName() {
        return userName;
    }

    /** 获取用户优先级 */
    public int getPriority() {
        return priority;
    }

    /** 获取登录时间戳 */
    public long getLoginTimestamp() {
        return loginTimestamp;
    }

    /** 返回用户的字符串表示 */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", priority=" + priority +
                ", loginTimestamp=" + loginTimestamp +
                '}';
    }

    /** 判断两个用户是否相等（基于userId） */
    @Override
    public boolean equals(Object o) {
        if (this == o) {  // 引用相同，直接返回true
            return true;
        }
        if (o == null || getClass() != o.getClass()) {  // 对象为空或类型不同
            return false;
        }
        User user = (User) o;  // 强制类型转换
        return userId == user.userId;  // 比较userId
    }

    /** 计算用户的哈希值（基于userId） */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}