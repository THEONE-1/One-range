package com.one.range.controller.basevul.xxe;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Student 实体类，用于 XXE Unmarshaller 漏洞演示
 */
@XmlRootElement(name = "student")
public class Student {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student{name='" + name + "'}";
    }
}
