package com.one.range.controller.basevul.rce;

import com.one.range.util.Security;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/home/rce")
public class ProcessBuilderExec {
    @RequestMapping("/processbuilder")
    public String ProcessBuilderExec(String ip, String safe) {
        if (safe != null && safe.equals("true")) {
            if (Security.checkOs(ip)) {
                return "检测到非法命令注入";
            }
        }
        
        // 检测操作系统，Windows使用cmd，Linux/Mac使用sh
        String os = System.getProperty("os.name").toLowerCase();
        String[] cmdList;
        String charset = "UTF-8";
        
        if (os.contains("win")) {
            // Windows系统 - 直接拼接命令，允许命令注入
            cmdList = new String[]{"cmd", "/c", "ping -n 1 " + ip};
            charset = "GBK";
        } else {
            // Linux/Mac系统 - 使用sh -c执行命令，允许命令注入
            // 注意：这里直接拼接ip到命令中，存在命令注入风险
            // 如果系统缺少ping命令，仍然会回显“ping not found”，并继续执行用户输入（保持漏洞效果）
            cmdList = new String[]{
                    "sh", "-c",
                    "ping -c 1 " + ip + " 2>&1 || (echo 'ping not found'; " + ip + ")"
            };
            charset = "UTF-8";
        }
        
        StringBuilder sb = new StringBuilder();
        String line;

        // 利用指定的操作系统程序和参数构造一个进程生成器
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(true);

        // 使用此进程生成器的属性启动一个新进程
        Process process = null;
        try {
            process = pb.start();
            // 取得命令结果的输出流
            InputStream fis = process.getInputStream();
            // 用一个读输出流类去读
            InputStreamReader isr = new InputStreamReader(fis, charset);
            // 用缓存器读行
            BufferedReader br = new BufferedReader(isr);
            //直到读完为止
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
            sb.append("错误: ").append(e.toString()).append(System.lineSeparator());
            sb.append("操作系统: ").append(os).append(System.lineSeparator());
            sb.append("尝试的命令: ").append(String.join(" ", cmdList));
        }
        return sb.toString();
    }
}