package lmy.agent.entity;

import java.util.ArrayList;
import java.util.List;

public class FileSnapshot {
    // 文件绝对路径
    private String filePath;
    // 文件最后修改时间戳
    private long lastModified;
    // 该文件切片写入向量库后的文档 ID
    private List<String> documentIds = new ArrayList<>();

    public FileSnapshot() {
    }

    public FileSnapshot(String filePath, long lastModified) {
        this.filePath = filePath;
        this.lastModified = lastModified;
    }

    public FileSnapshot(String filePath, long lastModified, List<String> documentIds) {
        this.filePath = filePath;
        this.lastModified = lastModified;
        this.documentIds = new ArrayList<>(documentIds);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public List<String> getDocumentIds() {
        return new ArrayList<>(documentIds);
    }

    public void setDocumentIds(List<String> documentIds) {
        this.documentIds = documentIds == null ? new ArrayList<>() : new ArrayList<>(documentIds);
    }
}