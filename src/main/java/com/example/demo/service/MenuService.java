package com.example.demo.service;

import com.example.demo.entity.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> findMenusByUserId(Integer userId);
}
