package com.one.range.controller.basevul.rce;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/home/rce")
public class RuntimeExec {
    @RequestMapping("/runtime")
    public String RuntimeExec(String cmd) {
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            Process proc = Runtime.getRuntime().exec(cmd);

            InputStream fis = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis, "GBK");
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }

        } catch (IOException e) {
            e.printStackTrace();
            sb.append("错误: ").append(e.toString());
        }
        return sb.toString();
    }
}
