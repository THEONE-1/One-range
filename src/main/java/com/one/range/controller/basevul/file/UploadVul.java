package com.one.range.controller.basevul.file;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/home/file")
public class UploadVul {

    private static final String UPLOADED_FOLDER = System.getProperty("user.dir") + "/src/main/resources/static/upload/";

    @PostMapping("/upload")
    public String uFile(@RequestParam("file") MultipartFile file, String check, HttpServletRequest httpServletRequest) {
        if (file.isEmpty()) {
            return "请选择要上传的文件!";
        }

        String fileName = file.getOriginalFilename();

        if (check != null && check.equals("true")) {
            // 获取文件后缀名，并且索引到最后一个，避免使用.jpg.jsp来绕过
            if (fileName == null || !fileName.contains(".")) {
                return "请勿HACK! 文件名无效";
            }
            String Suffix = fileName.substring(fileName.lastIndexOf("."));

            String[] SuffixSafe = {".jpg", ".png", ".jpeg", ".gif", ".bmp", ".ico"};
            boolean flag = false;

            // 如果满足后缀名单，返回true
            for (String s : SuffixSafe) {
                if (Suffix.toLowerCase().equals(s)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return "请勿HACK! 不允许的文件类型";
            }
        }

        try {
            byte[] bytes = file.getBytes();
            Path dir = Paths.get(UPLOADED_FOLDER);
            Path path = Paths.get(UPLOADED_FOLDER + fileName);

            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.write(path, bytes);

            String urlPath = httpServletRequest.getContextPath();
            String scheme = httpServletRequest.getScheme();
            String serverName = httpServletRequest.getServerName();
            int port = httpServletRequest.getServerPort();
            String basePath = scheme + "://" + serverName + ":" + port + urlPath;

            return "上传成功" + System.lineSeparator() + "文件路径: " + path + System.lineSeparator() + "访问地址: " + basePath + "/upload/" + fileName;
        } catch (Exception e) {
            return "上传失败: " + e.toString();
        }
    }
}
