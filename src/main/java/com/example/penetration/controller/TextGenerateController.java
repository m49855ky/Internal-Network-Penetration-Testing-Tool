package com.example.penetration.controller;


import com.example.penetration.service.TextGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

@RestController
@RequestMapping("/text")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TextGenerateController {
    private final TextGeneratorService textGeneratorService;
    @GetMapping("/doc")
    public ResponseEntity<byte[]> doc(@RequestParam Integer userId, HttpServletRequest request) throws IOException {
        // 1. 生成文件
        File tempFile = File.createTempFile("report_", ".docx");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            textGeneratorService.run(userId, out);
        }
        byte[] fileContent = Files.readAllBytes(tempFile.toPath());

        // 2. 动态处理文件名编码（兼容所有浏览器）
        String filename = "渗透攻击报告.docx";
        String userAgent = request.getHeader("User-Agent");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(fileContent.length);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        // 关键：处理不同浏览器的文件名编码
        if (userAgent != null && (userAgent.contains("MSIE") || userAgent.contains("Trident") || userAgent.contains("Edge"))) {
            // IE/Edge 浏览器
            filename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        } else {
            // 其他浏览器（Chrome/Firefox/Safari），直接使用URL编码
            filename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        }
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename)
                .build());

        // 3. 允许前端读取Content-Disposition头（解决跨域）
        headers.setAccessControlExposeHeaders(Collections.singletonList("Content-Disposition"));

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }
}