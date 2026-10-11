package lmy.agent.utils;

import cn.hutool.core.bean.BeanUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lmy.agent.vo.req.LoginReq;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static Map<String,String> account = new HashMap<>();
    //生成令牌
    public static String getToken(LoginReq clasims) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(clasims);
        return JWT.create()
                .withClaim("init", stringObjectMap)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 12))//过期时间
                .sign(Algorithm.HMAC256("init"));//指定密钥
    }

    //解析令牌
    public static LoginReq parseToken(String token) {
        Map<String, Object> map = JWT.require(Algorithm.HMAC256("init"))
                .build()
                .verify(token)
                .getClaim("init")
                .asMap();
        return BeanUtil.mapToBean(map, LoginReq.class, false);
    }
}