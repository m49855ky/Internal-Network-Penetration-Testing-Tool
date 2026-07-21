package com.example.penetration.utils;


import com.example.penetration.entity.IPScanEntity;
import com.example.penetration.entity.OSScanEntity;
import com.example.penetration.entity.PortScanEntity;
import com.example.penetration.websocket.CommandWebSocket;
import com.example.penetration.websocket.WebsocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NmapUtils {
    private final CommandWebSocket webSocket;
    private final ObjectMapper objectMapper;
    private final CommandExecutor commandExecutor;
    private Set<String> onlineIpsCache = new HashSet<>();
    public List<IPScanEntity> readNmap(String ipRange) {
        List<IPScanEntity> list = initIpRange(ipRange);
        String command = commandExecutor.executeCommand("nmap -sn " + ipRange);

        // 清空缓存
        onlineIpsCache.clear();

        // 调试：打印 nmap 原始输出
        //System.out.println("Nmap raw output:\n" + command);

        // 正则表达式：直接提取 IP，忽略主机名
        String regex = "Nmap scan report for .*?(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        // 提取所有在线 IP
        while (matcher.find()) {
            String ip = matcher.group(1);
            onlineIpsCache.add(ip);
        }

        // 更新 list 中的 IP 状态
        for (IPScanEntity entity : list) {
            if (onlineIpsCache.contains(entity.getIp())) {
                entity.setStatus("Online");
                entity.setCreateTime(LocalDateTime.now());
            }
        }

        return list;
    }

    // 获取所有在线IP
    public Set<String> getOnlineIps() {
        return new HashSet<>(onlineIpsCache);
    }

    // 新增方法：批量扫描操作系统
    @Async("taskExecutor")
    public CompletableFuture<List<OSScanEntity>> batchScanOS() {
        List<OSScanEntity> results = new ArrayList<>();
        for (String ip : onlineIpsCache) {
            results.add(readOS(ip));
        }
        return CompletableFuture.completedFuture(results);
    }


    public OSScanEntity readOS(String ip) {
        OSScanEntity osEntity = new OSScanEntity();
        osEntity.setIp(ip);
        osEntity.setCreateTime(LocalDateTime.now());
        String command = commandExecutor.executeCommand("nmap -O " + ip);
        // 匹配多种可能的操作系统识别结果
        String regex = "(Running: (.+))|(Aggressive OS guesses: (.+))|(OS details: (.+))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);
        while (matcher.find()) {
            // 检查不同匹配组
            String os = matcher.group(2);  // Running: 匹配组
            if (os == null) {
                os = matcher.group(4);     // Aggressive OS guesses: 匹配组
            }
            if (os == null) {
                os = matcher.group(6);     // OS details: 匹配组
            }

            if (os != null) {
                osEntity.setOs(os.trim());  // 去除前后空格
                osEntity.setCreateTime(LocalDateTime.now());
                break;  // 找到第一个匹配即可
            }
        }
        return osEntity;
    }

    public List<PortScanEntity> readPort(String ip) {
        List<PortScanEntity> list = new ArrayList<>();
        String command = commandExecutor.executeCommand("nmap -O " + ip);
        String[] split = command.split("\n");
        Pattern pattern = Pattern.compile("(\\d+)/(tcp|udp)\\s+(open)\\s+(\\S+)");
        for (String line : split) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                // 解析匹配到的端口信息
                String port = matcher.group(1);   //端口号
                String protocol = matcher.group(2);  //协议类型（tcp/udp）
                String status = matcher.group(3);   // 端口状态（open）
                String server = matcher.group(4);  // 服务类型，如http等
                PortScanEntity portScanEntity = new PortScanEntity();  //创建 PortScanEntity 对象并设置属性
                portScanEntity.setIp(ip);
                portScanEntity.setPort(Integer.parseInt(port));
                portScanEntity.setProtocol(protocol);
                portScanEntity.setCreateTime(LocalDateTime.now());
                portScanEntity.setStatus(status);
                portScanEntity.setType(server);
                list.add(portScanEntity);
            }
        }
        return list;
    }

    @Async("taskExecutor")
    public CompletableFuture<String> readVulnerabilities(String ip) throws Exception {
        // 生成带时间戳的xml文件名
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String filename = now.format(formatter) + ".xml";

        // 准备输出目录
        String outputDir = "src/main/resources/";
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path filePath = outputPath.resolve(filename);
        String command = "nmap --script=vuln -oX " + filePath + " " + ip;

        // 异步执行扫描命令
        commandExecutor.command(command);

        // 通知前端WebSocket扫描完成
        WebsocketResponse response = new WebsocketResponse();
        response.setData(filePath.toAbsolutePath().toString());
        response.setType("vulnerability");
        webSocket.sendAll(objectMapper.writeValueAsString(response));

        return CompletableFuture.completedFuture(filePath.toString());
    }

    public static List<IPScanEntity> initIpRange(String ipRange) {
        List<IPScanEntity> list = new ArrayList<>();
        String startIp = ipRange.split("-")[0];  // 获取起始IP（如192.168.1.1）
        String endIp = startIp.split("\\.")[0] + "." + startIp.split("\\.")[1] + "." + startIp.split("\\.")[2] + "." + ipRange.split("-")[1];  // 构建结束IP（如192.168.1.254）
        String prefix = startIp.split("\\.")[0] + "." + startIp.split("\\.")[1] + "." + startIp.split("\\.")[2] + ".";     // 构建IP前缀（如192.168.1.）
        for (int i = Integer.parseInt(startIp.split("\\.")[3]); i <= Integer.parseInt(endIp.split("\\.")[3]); i++) {
            IPScanEntity entity = new IPScanEntity();
            entity.setIp(prefix + i);
            list.add(entity);
            entity.setStatus("offline");
            entity.setCreateTime(LocalDateTime.now());
        }
        return list;
    }
}