package com.example.demo.service;

import com.example.demo.entity.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

public interface NoticeService {
    Notice selectByPrimaryKey(Integer id);

    int insert(Notice record);

    int deleteByPrimaryKey(Integer id);
}
