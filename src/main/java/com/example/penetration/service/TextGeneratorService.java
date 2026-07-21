package com.example.penetration.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.penetration.entity.MsfEntity;
import com.example.penetration.entity.OSScanEntity;
import com.example.penetration.entity.PortScanEntity;
import com.example.penetration.entity.Vulnerability;
import com.example.penetration.mapper.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TextGeneratorService {
    private final OSScanMapper osScanMapper;
    private final PortScanMapper portScanMapper;
    private final VulnerabilityMapper vulnerabilityMapper;
    private final MsfMapper msfMapper;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void run(Integer userId, OutputStream outputStream) throws IOException {
        XWPFDocument doc = new XWPFDocument();
        try {
            // 初始化文档内容
            init(doc, userId);
            messageTable(doc, userId);
            addPortScanTables(doc, userId);
            addVulnerabilityTables(doc, userId);
            addMsfExploitationResults(doc, userId);

            // 将文档写入输出流
            doc.write(outputStream);
        } finally {
            doc.close();
        }
        System.out.println("渗透攻击报告生成完成!");
    }

    public void init(XWPFDocument doc, Integer userId) {
        XWPFParagraph titleParagraph = doc.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("渗透攻击报告");
        titleRun.setFontSize(20);
        titleRun.setBold(true);
    }

    public void messageTable(XWPFDocument doc, Integer userId) {
        // 添加基本信息表(OS扫描结果)
        QueryWrapper<OSScanEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<OSScanEntity> osScanEntityList = osScanMapper.selectList(queryWrapper);

        if (osScanEntityList == null || osScanEntityList.isEmpty()) {
            // No results found
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("没有找到操作系统扫描结果");
            return;
        }
        // 创建表格
        XWPFTable basicInfoTable = doc.createTable();
        basicInfoTable.setWidth("100%");//设置表格宽度
        XWPFTableRow headerRow = basicInfoTable.getRow(0);
        while (headerRow.getTableCells().size() < 3) {
            headerRow.addNewTableCell();
        }
        setHeaderCell(headerRow.getCell(0), "IP地址");
        setHeaderCell(headerRow.getCell(1), "操作系统");
        setHeaderCell(headerRow.getCell(2), "扫描时间");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (OSScanEntity osScanEntity : osScanEntityList) {
            XWPFTableRow row = basicInfoTable.createRow();
            // IP address
            row.getCell(0).setText(osScanEntity.getIp() != null ? osScanEntity.getIp() : "");
            // OS information
            row.getCell(1).setText(osScanEntity.getOs() != null ? osScanEntity.getOs() : "未知");
            // Scan time
            String scanTime = osScanEntity.getCreateTime() != null
                    ? osScanEntity.getCreateTime().format(formatter)
                    : "时间未知";
            row.getCell(2).setText(scanTime);
        }
        // Add some space after the table
        doc.createParagraph().setSpacingAfter(400);
    }


    public void addPortScanTables(XWPFDocument doc, Integer userId) {
        // 添加端口扫描结果
        List<PortScanEntity> portScans = portScanMapper.selectList(new QueryWrapper<PortScanEntity>()
                .eq("user_id", userId)
                .orderByAsc("ip", "port"));

        if (portScans == null || portScans.isEmpty()) {
            // No results found
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("No port scan results found for this user.");
            return;
        }
        // Group results by IP address
        Map<String, List<PortScanEntity>> scansByIp = portScans.stream()
                .collect(Collectors.groupingBy(PortScanEntity::getIp));
        // Create a table for each IP
        for (Map.Entry<String, List<PortScanEntity>> entry : scansByIp.entrySet()) {
            String ip = entry.getKey();
            List<PortScanEntity> ipScans = entry.getValue();
            // Add IP title
            XWPFParagraph ipPara = doc.createParagraph();
            XWPFRun ipRun = ipPara.createRun();
            ipRun.setText(ip + " 的端口扫描结果");
            ipRun.setBold(true);
            ipRun.setFontSize(14);
            ipPara.setSpacingAfter(200); // Add some space after the title
            // Create table
            XWPFTable table = doc.createTable();
            // Set table style (optional)
            table.setWidth("100%");
            // Create header row
            XWPFTableRow headerRow = table.getRow(0);
            if (headerRow.getTableCells().size() < 5) {
                // Ensure we have enough cells
                for (int i = headerRow.getTableCells().size(); i < 5; i++) {
                    headerRow.addNewTableCell();
                }
            }
            // Set header cells
            setHeaderCell(headerRow.getCell(0), "端口");
            setHeaderCell(headerRow.getCell(1), "状态");
            setHeaderCell(headerRow.getCell(2), "协议");
            setHeaderCell(headerRow.getCell(3), "类型");
            setHeaderCell(headerRow.getCell(4), "扫描时间");
            // Add data rows
            for (PortScanEntity scan : ipScans) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(scan.getPort().toString());
                row.getCell(1).setText(scan.getStatus());
                row.getCell(2).setText(scan.getProtocol());
                row.getCell(3).setText(scan.getType());
                row.getCell(4).setText(scan.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            // add some space after the table
            doc.createParagraph().setSpacingAfter(400);
        }
    }

    //设置表头单元格样式
    private void setHeaderCell(XWPFTableCell cell, String text) {
        cell.removeParagraph(0);
        XWPFParagraph para = cell.addParagraph();
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(true); //加粗
        cell.setColor("D3D3D3"); // 灰色背景
    }

    public void addVulnerabilityTables(XWPFDocument doc, Integer userId) {
        //添加漏洞扫描结果
        List<Vulnerability> vulnerabilities = vulnerabilityMapper.selectList(new QueryWrapper<Vulnerability>()
                .eq("user_id", userId)
                .orderByAsc("ip", "create_time"));
        if (vulnerabilities == null || vulnerabilities.isEmpty()) {
            // No results found
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("No vulnerability results found for this user.");
            return;
        }
        // Group results by IP address
        Map<String, List<Vulnerability>> vulnsByIp = vulnerabilities.stream()
                .collect(Collectors.groupingBy(Vulnerability::getIp));
        // Create a table for each IP
        for (Map.Entry<String, List<Vulnerability>> entry : vulnsByIp.entrySet()) {
            String ip = entry.getKey();
            List<Vulnerability> ipVulns = entry.getValue();
            // Add IP title
            XWPFParagraph ipPara = doc.createParagraph();
            XWPFRun ipRun = ipPara.createRun();
            ipRun.setText(ip + " 的漏洞扫描结果");
            ipRun.setBold(true);
            ipRun.setFontSize(14);
            ipPara.setSpacingAfter(200); // Add some space after the title
            // Create table
            XWPFTable table = doc.createTable();
            table.setWidth("100%");
            // Create header row with 5 columns
            XWPFTableRow headerRow = table.getRow(0);
            while (headerRow.getTableCells().size() < 5) {
                headerRow.addNewTableCell();
            }
            // Set header cells
            setHeaderCell(headerRow.getCell(0), "漏洞名称");
            setHeaderCell(headerRow.getCell(1), "标题");
            setHeaderCell(headerRow.getCell(2), "状态");
            setHeaderCell(headerRow.getCell(3), "描述");
            setHeaderCell(headerRow.getCell(4), "发现时间");
            // Add data rows
            for (Vulnerability vuln : ipVulns) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(vuln.getName() != null ? vuln.getName() : "");
                row.getCell(1).setText(vuln.getTitle() != null ? vuln.getTitle() : "");
                row.getCell(2).setText(vuln.getStatus() != null ? vuln.getStatus() : "");
                // Handle potentially long description text
                String description = vuln.getDescription() != null ? vuln.getDescription() : "";
                if (description.length() > 100) {
                    description = description.substring(0, 100) + "..."; // Truncate long descriptions
                }
                row.getCell(3).setText(description);
                row.getCell(4).setText(vuln.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            // Add some space after the table
            doc.createParagraph().setSpacingAfter(400);
        }
    }

    public void addMsfExploitationResults(XWPFDocument doc, Integer userId) {
        // 添加Metasploit渗透结果
        List<MsfEntity> msfResults = msfMapper.selectList(new QueryWrapper<MsfEntity>()
                .eq("user_id", userId)
                .orderByAsc("ip", "create_time"));

        if (msfResults == null || msfResults.isEmpty()) {
            // No results found
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("没有找到Metasploit渗透测试结果");
            return;
        }

        // Group results by IP address
        Map<String, List<MsfEntity>> resultsByIp = msfResults.stream()
                .collect(Collectors.groupingBy(MsfEntity::getIp));

        // Create a section for each IP
        for (Map.Entry<String, List<MsfEntity>> entry : resultsByIp.entrySet()) {
            String ip = entry.getKey();
            List<MsfEntity> ipResults = entry.getValue();

            // Add IP title
            XWPFParagraph ipPara = doc.createParagraph();
            XWPFRun ipRun = ipPara.createRun();
            ipRun.setText(ip + " 的Metasploit渗透测试结果");
            ipRun.setBold(true);
            ipRun.setFontSize(14);
            ipPara.setSpacingAfter(200);

            // Add details for each exploitation attempt
            for (MsfEntity result : ipResults) {
                // Vulnerability name
                XWPFParagraph vulnPara = doc.createParagraph();
                XWPFRun vulnRun = vulnPara.createRun();
                vulnRun.setText("漏洞名称: " + (result.getVulnerabilityName() != null ? result.getVulnerabilityName() : "未知"));
                vulnRun.setBold(true);

                // Module used
                XWPFParagraph modulePara = doc.createParagraph();
                XWPFRun moduleRun = modulePara.createRun();
                moduleRun.setText("使用模块: " + (result.getVulnerabilityModule() != null ? result.getVulnerabilityModule() : "未知"));

                // Process/result
                XWPFParagraph processPara = doc.createParagraph();
                XWPFRun processRun = processPara.createRun();
                processRun.setText("执行结果: " + (result.getProcess() != null ? result.getProcess() : "未知"));

                // Time
                XWPFParagraph timePara = doc.createParagraph();
                XWPFRun timeRun = timePara.createRun();
                timeRun.setText("执行时间: " + (result.getCreateTime() != null ?
                        result.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "时间未知"));

                // Add separator between entries
                XWPFParagraph separator = doc.createParagraph();
                XWPFRun separatorRun = separator.createRun();
                separatorRun.setText("———————————————————————");
                separatorRun.setColor("808080"); // Gray color
                separator.setSpacingAfter(200);
            }
        }

        // Add some space after the section
        doc.createParagraph().setSpacingAfter(400);
    }

}
