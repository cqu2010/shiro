package com.example.demo.service;

import com.example.demo.entity.User;

public interface UserService {
    User getUserById(Integer id);

    User findByName(String userName);
}
