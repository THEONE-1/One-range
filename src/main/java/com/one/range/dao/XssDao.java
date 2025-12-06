package com.one.range.dao;

import org.apache.ibatis.annotations.Mapper;
import com.one.range.model.Xss;

import java.util.List;

@Mapper
public interface XssDao {
    List<String> getContent();

    void setContent(Xss xss);

    void clear();
}
