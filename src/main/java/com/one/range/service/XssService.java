package com.one.range.service;

import com.one.range.model.Xss;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface XssService {
    List<String > getContent();

    void setContent(Xss xss);

    void clear();
}
