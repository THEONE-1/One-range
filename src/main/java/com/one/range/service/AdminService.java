package com.one.range.service;

import com.one.range.model.Admin;

import java.util.ArrayList;

public interface AdminService {
     Admin login(String username, String password);

     int updatePWD(String username, String newPWD);

     Admin getInfoByUserName(String username);

     ArrayList<Admin> getAllInfo();

     /**
      * 用户注册
      */
     int register(String username, String password);

     /**
      * 检查用户名是否存在
      */
     boolean checkUsernameExists(String username);

     /**
      * 根据ID获取用户信息
      */
     Admin getInfoById(Integer id);

     /**
      * 删除用户
      */
     int deleteUser(Integer id);
}
