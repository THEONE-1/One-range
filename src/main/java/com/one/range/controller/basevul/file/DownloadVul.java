package com.one.range.controller.basevul.file;

import com.one.range.util.Security;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;

@RestController
@RequestMapping("/home/file")
public class DownloadVul {

    @RequestMapping("/download")
    public String readFile(String filename, String check, HttpServletResponse response) {
        if (check != null && check.equals("true")) {
            if (Security.checkTraversal(filename)) {
                return "请勿输入非法文件名!";
            }
        }

        String filePath = System.getProperty("user.dir") + "/src/main/resources/static/upload/" + filename;

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "文件不存在: " + filename;
            }
            InputStream fis = new BufferedInputStream(Files.newInputStream(file.toPath()));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            response.reset();
            response.addHeader("Content-Disposition", "attachment;filename=" + filename);
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
            return "文件下载成功: " + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return "下载失败: " + e.toString();
        }
    }
}
