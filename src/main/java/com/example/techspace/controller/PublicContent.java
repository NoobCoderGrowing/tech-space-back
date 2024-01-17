package com.example.techspace.controller;

import com.example.techspace.ArticleRepository;
import com.example.techspace.entity.Article;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;
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

    @RequestMapping(value = "/retrieve/articleList", method = RequestMethod.GET)
    @ResponseBody
    public Set<String> retrieveArticleList(){
        articleLock.readLock().lock();
        try {
            ConcurrentHashMap<String, String> titleIDMap = articleMap.get("tech");
            Set<String> titleSet = titleIDMap.keySet();
            return titleSet;
        }finally {
            articleLock.readLock().unlock();
        }
    }

    @RequestMapping(value = "/retrieve/articleByTitle", method = RequestMethod.GET)
    @ResponseBody
    public Article retrieveArticleByTitle(@RequestParam String title){
        articleLock.readLock().lock();
        try {
            ConcurrentHashMap<String, String> titleIDMap = articleMap.get("tech");
            String id = titleIDMap.get(title);
            Article article = articleRepository.findById(id).get();
            return article;
        }finally {
            articleLock.readLock().unlock();
        }

    }


}
