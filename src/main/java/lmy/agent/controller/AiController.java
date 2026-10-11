package lmy.agent.controller;

import lmy.agent.service.RagFileLoaderService;
import lmy.agent.vo.resp.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static lmy.agent.vo.resp.Result.error;
import static lmy.agent.vo.resp.Result.success;


@RequestMapping("/cache")
@RestController
public class AiController {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private RagFileLoaderService ragFileLoaderService;


    @GetMapping("/ai/reloadFolder")
    public Result reloadFolder() {
        try {
            ragFileLoaderService.reloadFolder();
        }catch (Exception e){
            return error("刷新失败");
        }
        return success("刷新成功");
    }

    @GetMapping("/ai/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .system("""
                        你是一个项目智能助手。
                        用户普通聊天时直接回答即可。
                        只有当用户的问题需要查询项目文档、业务规则、获取当前登陆人的信息、系统说明或知识库资料时，才调用 searchKnowledge 工具，调用 searchKnowledge 后，只能基于工具返回的内容回答，不要编造知识库中没有的信息。
                        询问当前时间时调用 getCurrentTime。
                        询问机器信息时调用 getMachineInfo。
                        """)
                .user(message)
                .call()
                .content();
    }

    @GetMapping(value = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String message, @RequestParam(defaultValue = "default") String conversationId) {
        return chatClient.prompt()
                .system("""
                        你是一个项目智能助手。
                        只有当用户的问题需要查询项目文档、业务规则、获取当前登陆人的信息、系统说明或知识库资料时，才调用 searchKnowledge 工具。
                        普通聊天直接回答；询问当前时间时调用 getCurrentTime；
                        询问机器信息时调用 getMachineInfo。
                        调用 searchKnowledge 后，只能基于工具返回的内容回答，不要编造知识库中没有的信息。
                        """)
                .user(message)
                .advisors(advisor -> advisor.param(
                        "chat_memory_conversation_id",
                        conversationId
                ))
                .stream()
                .content();
    }
}
