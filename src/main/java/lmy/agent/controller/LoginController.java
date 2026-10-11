package lmy.agent.controller;

import lmy.agent.vo.req.LoginReq;
import lmy.agent.vo.resp.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static lmy.agent.utils.JwtUtil.getToken;
import static lmy.agent.vo.resp.Result.error;
import static lmy.agent.vo.resp.Result.success;

@RequestMapping("/login")
@RestController
public class LoginController {

    @GetMapping("/loginCheck")
    public Result<String> reloadFolder(LoginReq loginReq) {
        if (!loginReq.getAccount().equals("admin") || !loginReq.getPassword().equals("admin")) {
            return error("不存在当前用户~");
        }
        String token = getToken(loginReq);
        return success(token);
    }
}
