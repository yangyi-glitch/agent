package lmy.agent.tools;

import lmy.agent.service.RagFileLoaderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeBaseTools {

    private final RagFileLoaderService ragFileLoaderService;

    public KnowledgeBaseTools(RagFileLoaderService ragFileLoaderService) {
        this.ragFileLoaderService = ragFileLoaderService;
    }

    @Tool(description = """
            查询项目知识库。
            当用户询问项目文档、业务规则、系统说明、知识库资料、沧州师范学院信息或奉节美食资料时调用。
            普通聊天、当前时间和机器信息问题不要调用此工具。
            """)
    public String searchKnowledge(
            @ToolParam(description = "用户想从知识库中查询的问题") String question) {
        return ragFileLoaderService.searchKnowledge(question, 3);
    }
}
