package lmy.agent.utils;

import lmy.agent.vo.req.LoginReq;

public class ThreadLocalUtil {
    //提供ThreadLocal对象
    private static final ThreadLocal<LoginReq> THREAD_LOCAL = new ThreadLocal();

    //根据键获取值
    public static LoginReq getLoginUser() {
        return THREAD_LOCAL.get();
    }

    //存储键值对
    public static void set(LoginReq value) {
        THREAD_LOCAL.set(value);
    }

    //清除ThreadLocal防止内存泄露
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}