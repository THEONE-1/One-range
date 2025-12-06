package com.one.range.service;

import com.one.range.model.Admin;

import java.util.ArrayList;

public interface InjectService {
    ArrayList<Admin> orderBy(String field);

    Admin likeSearch(String username);

//    ArrayList<Admin> in(List<String> ids);
    ArrayList<Admin> in(String ids);
}
