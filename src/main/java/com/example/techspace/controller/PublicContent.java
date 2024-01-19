package com.example.techspace.controller;

import com.example.techspace.ArticleRepository;
import com.example.techspace.entity.Article;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/public")
@EnableMethodSecurity
public class PublicContent {

    @Resource
    ArticleRepository articleRepository;

    @Autowired
    @Qualifier("articleMap")
    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> articleMap;

    @Autowired
    @Qualifier("articleLock")
    ReadWriteLock articleLock;

    @RequestMapping(value = "/retrieve/articles", method = RequestMethod.GET)
    @ResponseBody
    public List<Article> retrieveArticles(){
        List<Article> articles = articleRepository.findAll();
        return articles;
    }

    @RequestMapping(value = "/retrieve/articleMap", method = RequestMethod.GET)
    @ResponseBody
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> retrieveArticleMap(){
        articleLock.readLock().lock();
        try {
            return articleMap;
        }finally {
            articleLock.readLock().unlock();
        }
    }

    @RequestMapping(value = "/retrieve/articleByTitleCategory", method = RequestMethod.GET)
    @ResponseBody
    public Article retrieveArticleByTitle(@RequestParam String title, @RequestParam String category){
        articleLock.readLock().lock();
        try {
            ConcurrentHashMap<String, String> titleIDMap = articleMap.get(category);
            String id = titleIDMap.get(title);
            Article article = articleRepository.findById(id).get();
            return article;
        }finally {
            articleLock.readLock().unlock();
        }
    }


}
