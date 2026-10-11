package lmy.agent.service;

import lmy.agent.entity.FileSnapshot;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagFileLoaderService {

    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private TokenTextSplitter splitter;
    @Value("${app.rag.folder-path}")
    private String folderPath;
    // 内存快照：key=文件绝对路径，value=文件快照信息
    private final Map<String, FileSnapshot> fileSnapshotMap = new HashMap<>();

    /**
     * 扫描目录，增量更新向量库（只处理变更文件）
     *
     * @return 本次处理结果描述
     */
    public String reloadFolder() {
        File dir = new File(folderPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("目录不存在：" + folderPath);
        }

        // 1. 扫有描当前目录下所支持的文件
        Map<String, File> currentFileMap = new HashMap<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && (f.getName().endsWith(".txt") || f.getName().endsWith(".md"))) {
                    currentFileMap.put(f.getAbsolutePath(), f);
                }
            }
        }

        List<String> toDeleteDocIds = new ArrayList<>();
        List<Document> newChunks = new ArrayList<>();

        // 2. 找出【已经被删除】的文件，删除它对应的向量
        Set<String> snapshotFilePaths = new HashSet<>(fileSnapshotMap.keySet());
        for (String oldFilePath : snapshotFilePaths) {
            if (!currentFileMap.containsKey(oldFilePath)) {
                // VectorStore 没有通用的“查询全部文档”接口，按写入时保存的 ID 删除。
                toDeleteDocIds.addAll(fileSnapshotMap.get(oldFilePath).getDocumentIds());
                fileSnapshotMap.remove(oldFilePath);
            }
        }

        // 执行删除
        if (!toDeleteDocIds.isEmpty()) {
            vectorStore.delete(toDeleteDocIds);
        }

        // 3. 遍历当前目录文件，判断新增/修改
        for (Map.Entry<String, File> entry : currentFileMap.entrySet()) {
            String absPath = entry.getKey();
            File file = entry.getValue();
            long currentMtime = file.lastModified();

            FileSnapshot snapshot = fileSnapshotMap.get(absPath);
            // 新增文件 或者 文件修改时间发生变化
            if (snapshot == null || snapshot.getLastModified() != currentMtime) {
                // 先删除该文件上一次写入的分片。
                if (snapshot != null && !snapshot.getDocumentIds().isEmpty()) {
                    vectorStore.delete(snapshot.getDocumentIds());
                }

                // 读取文件，切片，增加metadata记录文件路径
                FileSystemResource resource = new FileSystemResource(file);
                TextReader reader = new TextReader(resource);
                List<Document> docs = reader.get();
                for (Document doc : docs) {
                    // 把文件路径写入元数据，用于后续删除该文件对应的分片
                    doc.getMetadata().put("filePath", absPath);
                }
                List<Document> chunks = splitter.apply(docs);
                newChunks.addAll(chunks);

                // 更新内存快照
                fileSnapshotMap.put(absPath, new FileSnapshot(
                        absPath,
                        currentMtime,
                        chunks.stream().map(Document::getId).toList()
                ));
            }
        }

        // 批量新增向量
        if (!newChunks.isEmpty()) {
            vectorStore.add(newChunks);
        }

        return String.format("完成增量更新：删除文档分片%d条，新增/更新分片%d条",
                toDeleteDocIds.size(), newChunks.size());
    }

    /**
     * 获取当前内存快照信息，用于调试查看已加载文件
     */
    public Map<String, FileSnapshot> getSnapshot() {
        return new HashMap<>(fileSnapshotMap);
    }

    /**
     * 清空向量库 + 清空快照
     */
    public String clearAll() {
        List<String> allIds = fileSnapshotMap.values().stream()
                .flatMap(snapshot -> snapshot.getDocumentIds().stream())
                .distinct()
                .toList();
        if (!allIds.isEmpty()) {
            vectorStore.delete(allIds);
        }
        fileSnapshotMap.clear();
        return "向量库与文件快照全部清空";
    }

    /**
     * 查询知识库并返回给 Tool 的结果。
     * 这里不负责拼接给模型的最终 Prompt，是否调用本方法由模型决定。
     */
    public String searchKnowledge(String userQuestion, int sum) {
        int topK = Math.max(1, Math.min(sum, 8));
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userQuestion)
                .topK(topK)
                .build();
        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);
        if (relevantDocs.isEmpty()) {
            return "知识库中未找到与问题相关的资料。";
        }
        if (!relevantDocs.isEmpty()) {
            for (int i = 0; i < relevantDocs.size(); i++) {
                System.out.println("<==============================分割线==============================>");
                System.out.println("分片【" + (i + 1) + "】");
                System.out.println(relevantDocs.get(i).toString());
            }
        }
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n=====\n"));
        //3. 组装prompt
        String promptTemplate = """
                你是知识库助手，请基于下面【知识库内容】回答用户问题。
                如果知识库没有相关信息，不要编造，直接回答：知识库中未找到相关资料。

                【知识库内容】
                {context}

                用户问题：{question}
                """;
        return promptTemplate.replace("{context}", context).replace("{question}", userQuestion);
    }
}
