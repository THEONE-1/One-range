package com.one.range.service.impl;

import com.one.range.dao.XssDao;
import com.one.range.model.Xss;
import com.one.range.service.XssService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XssServiceImpl implements XssService {
    final
    XssDao xssDao;

    public XssServiceImpl(XssDao xssDao) {
        this.xssDao = xssDao;
    }

    @Override
    public List<String> getContent() {
        return xssDao.getContent();
    }

    @Override
    public void setContent(Xss xss) {
        xssDao.setContent(xss);
    }

    @Override
    public void clear() {
        xssDao.clear();
    }
}
