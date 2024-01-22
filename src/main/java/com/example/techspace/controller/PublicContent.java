package com.example.techspace.controller;

import com.example.techspace.ArticleRepository;
import com.example.techspace.entity.Article;
import com.example.techspace.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/public")
@EnableMethodSecurity
public class PublicContent {

    @Resource
    ArticleService articleService;

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

    @RequestMapping(value = "/retrieve/articleByID", method = RequestMethod.POST)
    @ResponseBody
    public Article retrieveArticleByID(@RequestBody Map<String,String> request){
        articleLock.readLock().lock();
        try{
            String id = request.get("id");
            Article article = articleRepository.findById(id).get();
            return article;
        }finally {
            articleLock.readLock().unlock();
        }
    }

    @RequestMapping(value = "/delete/articleByID", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Boolean> deleteArticleByID(@RequestBody Map<String,String> request){
        articleLock.readLock().lock();
        try{
            HashMap<String, Boolean> response = new HashMap<String, Boolean>();
            String id = request.get("id");
            articleRepository.deleteById(id);
            articleService.hourlyUpdate();
            response.put("success", true);
            return response;
        }finally {
            articleLock.readLock().unlock();
        }
    }

    @RequestMapping(value = "/uodate/articles", method = RequestMethod.POST)
    @ResponseBody
    public String updateArticles(){
        articleLock.writeLock().lock();
        try{
            List<Article> articles = articleRepository.findAll();
            for (int i = 0; i < articles.size(); i++) {
                String id = articles.get(i).get_id();
                String title = articles.get(i).getTitle();
                if(title.contains("Learning to Rank")){
                    articles.get(i).setCategory("Learning to Rank");
                }else{
                    articles.get(i).setContent("Data Mining");
                }
                articleRepository.deleteById(id);;
                articleRepository.save(articles.get(i));
            }
            return "sucess";
        }finally {
            articleLock.writeLock().unlock();
        }
    }


}
