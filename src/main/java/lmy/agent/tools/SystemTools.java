package lmy.agent.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class SystemTools {

    private static final Logger log = LoggerFactory.getLogger(SystemTools.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = "获取当前时间")
    public String getCurrentTime() {
        try {
            String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
            log.info("Current time: {}", currentTime);
            return currentTime;
        } catch (Exception e) {
            log.error("Failed to get current time", e);
            return "无法获取时间: " + e.getMessage();
        }
    }

    @Tool(description = "获取机器信息")
    public String getMachineInfo() {
        try {
            StringBuilder info = new StringBuilder();

            // 操作系统信息
            info.append("操作系统: ").append(System.getProperty("os.name")).append("\n");
            info.append("系统版本: ").append(System.getProperty("os.version")).append("\n");
            info.append("系统架构: ").append(System.getProperty("os.arch")).append("\n");

            // Java 版本信息
            info.append("Java 版本: ").append(System.getProperty("java.version")).append("\n");
            info.append("Java 供应商: ").append(System.getProperty("java.vendor")).append("\n");

            // CPU 信息
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            int processorCount = osBean.getAvailableProcessors();
            info.append("处理器核心数: ").append(processorCount).append("\n");
            info.append("系统负载平均值: ").append(String.format("%.2f", osBean.getSystemLoadAverage())).append("\n");

            // 内存信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
            info.append("堆内存使用: ").append(heapUsed).append("MB / ").append(heapMax).append("MB\n");

            // 磁盘信息
            File root = new File("/");
            long totalSpace = root.getTotalSpace() / (1024 * 1024 * 1024);
            long freeSpace = root.getFreeSpace() / (1024 * 1024 * 1024);
            info.append("磁盘总容量: ").append(totalSpace).append("GB\n");
            info.append("磁盘可用空间: ").append(freeSpace).append("GB\n");

            // 用户信息
            info.append("用户名: ").append(System.getProperty("user.name")).append("\n");
            info.append("用户目录: ").append(System.getProperty("user.home")).append("\n");

            String machineInfo = info.toString();
            log.info("Machine information collected");
            return machineInfo;
        } catch (Exception e) {
            log.error("Failed to collect machine information", e);
            return "无法获取机器信息: " + e.getMessage();
        }
    }
}