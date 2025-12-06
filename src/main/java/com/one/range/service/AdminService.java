package com.one.range.service;

import com.one.range.model.Admin;

import java.util.ArrayList;

public interface AdminService {
     Admin login(String username, String password);

     int updatePWD(String username, String newPWD);

     Admin getInfoByUserName(String username);

     ArrayList<Admin> getAllInfo();
}
