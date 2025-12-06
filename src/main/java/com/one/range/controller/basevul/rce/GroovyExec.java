package com.one.range.controller.basevul.rce;
import groovy.lang.GroovyShell;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home/rce")
public class GroovyExec {

    @GetMapping("/groovy")
    public String groovyExec(String cmd) {
        GroovyShell shell = new GroovyShell();
        try {
            shell.evaluate(cmd);
            return "执行成功！！！";
        } catch (Exception e) {
            e.printStackTrace();
            return "错误: " + e.toString();
        }
    }
}
