package com.one.range.service.impl;


import com.one.range.dao.InjectDao;
import com.one.range.model.Admin;
import com.one.range.service.InjectService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InjectServiceImpl implements InjectService {
    final InjectDao injectDao;

    public InjectServiceImpl(InjectDao injectDao) {
        this.injectDao = injectDao;
    }

    @Override
    public ArrayList<Admin> orderBy(String username) {
        return injectDao.orderBy(username);
    }

    @Override
    public Admin likeSearch(String username) {
        return injectDao.likeSearch(username);
    }

    @Override
//    public ArrayList<Admin> in(List<String> ids) {
    public ArrayList<Admin> in(String ids) {
        return injectDao.in(ids);
    }
}
