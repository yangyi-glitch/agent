package lmy.agent.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lmy.agent.utils.ThreadLocalUtil;
import lmy.agent.vo.req.LoginReq;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static lmy.agent.utils.JwtUtil.parseToken;


@Component
public class RequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取 token
        String token = request.getHeader("Token");
        if (token == null || token.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请先登录~");
            return false;
        }
        try {
            // 使用 JwtUtils 解析 token
            LoginReq loginReq = parseToken(token);
            if (loginReq == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "token失效~");
                return false;
            }
            ThreadLocalUtil.set(loginReq);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "token失效~");
            return false;
        }
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        log.info("请求结束，释放资源 =====> {}", ThreadLocalUtils.getLoginUser().getUsername());
        System.out.println("请求结束，释放资源 =====>" + ThreadLocalUtil.getLoginUser().getAccount());
        ThreadLocalUtil.remove();
    }
}
