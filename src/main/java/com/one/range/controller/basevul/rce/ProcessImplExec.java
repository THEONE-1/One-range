package com.one.range.controller.basevul.rce;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Map;

@RestController
@RequestMapping("/home/rce")
public class ProcessImplExec {

    @RequestMapping("/processimpl")
    public String processImplExec(String cmd) {
        CharArrayWriter infoStream = null;
        try {
            Class<?> clazz = Class.forName("java.lang.ProcessImpl");
            Method method = clazz.getDeclaredMethod("start", String[].class, Map.class, String.class, ProcessBuilder.Redirect[].class, boolean.class);
            method.setAccessible(true);
            Process e = (Process) method.invoke(null, new String[]{cmd}, null, null, null, false);

            char[] bs = new char[2048];
            int readSize = 0;
            infoStream = new CharArrayWriter();
            InputStream inputStream = e.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
            while ((readSize = inputStreamReader.read(bs)) > 0) {
                infoStream.write(bs, 0, readSize);
            }
            return infoStream.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "错误: " + ex.toString();
        }
    }
}
