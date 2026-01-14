package com.one.range.dao;

import org.apache.ibatis.annotations.Mapper;
import com.one.range.model.Admin;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface AdminDao {
    Admin login(String username, String password);

    ArrayList<Admin> getAllInfo();

    Admin getInfoByUserName(String username);

    int updatePWD(String username, String newPWD);

    /**
     * 用户注册
     */
    int register(@Param("username") String username, @Param("password") String password);

    /**
     * 检查用户名是否存在
     */
    int checkUsernameExists(@Param("username") String username);

    /**
     * 根据ID获取用户信息
     */
    Admin getInfoById(@Param("id") Integer id);

    /**
     * 删除用户
     */
    int deleteUser(@Param("id") Integer id);
}
