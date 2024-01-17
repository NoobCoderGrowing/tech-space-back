package com.example.techspace.service;

import com.example.techspace.entity.Article;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.example.techspace.ArticleRepository;

@Service
public class ArticleService {

    @Resource
    ArticleRepository articleRepository;

    @Autowired
    @Qualifier("articleMap")
    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> articleMap;

    @Autowired
    @Qualifier("articleLock")
    ReadWriteLock articleLock;

    @Scheduled(cron = "0 0 * * * ?")
    @PostConstruct
    public void hourlyUpdate(){
        articleLock.writeLock().lock();
        try {
            articleMap.clear();
            ConcurrentHashMap<String, String> titleIDMap = new ConcurrentHashMap<>();
            List<Article> articles = articleRepository.findAll();
            for (Article article : articles) {
                String title = article.getTitle();
                String id = article.get_id();
                titleIDMap.put(title,id);
            }
            articleMap.put("tech", titleIDMap);
        }finally {
            articleLock.writeLock().unlock();
        }

    }
}
