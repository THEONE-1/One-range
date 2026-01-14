package com.one.range.service.impl;

import com.one.range.dao.AdminDao;
import com.one.range.model.Admin;
import com.one.range.service.AdminService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AdminServiceImpl implements AdminService {
    final
    AdminDao adminDao;

    public AdminServiceImpl(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    @Override
    public Admin login(String username, String password) {
        return adminDao.login(username, password);
    }

    @Override
    public int updatePWD(String username, String newPWD) {
       return adminDao.updatePWD(username, newPWD);
    }

    @Override
    public Admin getInfoByUserName(String username) {
        return this.adminDao.getInfoByUserName(username);
    }

    @Override
    public ArrayList<Admin> getAllInfo() {
        return this.adminDao.getAllInfo();
    }

    @Override
    public int register(String username, String password) {
        return adminDao.register(username, password);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return adminDao.checkUsernameExists(username) > 0;
    }

    @Override
    public Admin getInfoById(Integer id) {
        return adminDao.getInfoById(id);
    }

    @Override
    public int deleteUser(Integer id) {
        return adminDao.deleteUser(id);
    }
}

