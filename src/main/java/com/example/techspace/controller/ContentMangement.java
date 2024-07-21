package com.example.techspace.controller;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.techspace.ArticleRepository;
import com.example.techspace.entity.Article;
import com.example.techspace.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/admin")
@EnableMethodSecurity
public class ContentMangement {

    @Autowired
    @Qualifier("articleLock")
    ReadWriteLock articleLock;


    @Resource
    ArticleRepository articleRepository;

    @Autowired
    @Qualifier("articleMap")
    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> articleMap;

    @Resource
    ArticleService articleService;

    @RequestMapping(value = "/upload/article", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public HashMap<String, Boolean> uploadArticle(@RequestBody Map<String,String> submitArticle){
        String params = JSONObject.toJSONString(submitArticle);
        Article article = JSON.parseObject(params, Article.class);
        String title = article.getTitle();
        String category = article.getCategory();
        HashMap<String, Boolean> response = new HashMap<String, Boolean>();
        if (!articleMap.containsKey(category) || !articleMap.get(category).containsKey(title)){
            while(!articleLock.writeLock().tryLock()){}
            try {
                articleRepository.save(article);
                response.put("uploaded", true);
                return response;
            }finally {
                articleLock.writeLock().unlock();
                articleService.hourlyUpdate();
            }
        }
        response.put("uploaded", false);
        return response;
    }

    @RequestMapping(value = "/deleteAll", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public List<Article> deleteAll() {
        while(!articleLock.writeLock().tryLock()){}
        try {
            articleRepository.deleteAll();
            return articleRepository.findAll();
        }finally {
            articleLock.writeLock().unlock();
            articleService.hourlyUpdate();
        }
    }

    @RequestMapping(value = "/delete/articleByID", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Boolean> deleteArticleByID(@RequestBody Map<String,String> request){

        HashMap<String, Boolean> response = new HashMap<String, Boolean>();
        String id = request.get("id");
        while(!articleLock.writeLock().tryLock()){}
        try {
            articleRepository.deleteById(id);
            response.put("success", true);
            return response;
        }finally {
            articleLock.writeLock().unlock();
            articleService.hourlyUpdate();
        }
    }

    @RequestMapping(value = "/update/articles", method = RequestMethod.GET)
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
                    articles.get(i).setCategory("Data Mining");
                }
                articleRepository.deleteById(id);;
                articleRepository.save(articles.get(i));
            }
            return "sucess";
        }finally {
            articleLock.writeLock().unlock();
            articleService.hourlyUpdate();
        }

    }
}
