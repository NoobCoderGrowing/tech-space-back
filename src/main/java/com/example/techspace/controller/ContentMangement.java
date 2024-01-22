package com.example.techspace.controller;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.techspace.ArticleRepository;
import com.example.techspace.entity.Article;
import com.example.techspace.service.ArticleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/admin")
@EnableMethodSecurity
public class ContentMangement {



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
        System.out.println(params);
        Article article = JSON.parseObject(params, Article.class);
        String title = article.getTitle();
        String category = article.getCategory();
        HashMap<String, Boolean> response = new HashMap<String, Boolean>();
        if(!articleMap.get(category).containsKey(title)){
            articleRepository.save(article);
            articleService.hourlyUpdate();
            response.put("uploaded", true);
        }else{
            response.put("uploaded", false);
        }
        return response;
    }


    @RequestMapping(value = "/test", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public HashMap<String, Boolean> test(@RequestBody Map<String,String> submitArticle) {
        String params = JSONObject.toJSONString(submitArticle);
        System.out.println(params);
        Article article = JSON.parseObject(params, Article.class);
        String title = article.getTitle();
        String category = article.getCategory();
        HashMap<String, Boolean> response = new HashMap<String, Boolean>();
        if (!articleMap.containsKey(category) || !articleMap.get(category).containsKey(title)){
                articleRepository.save(article);
                articleService.hourlyUpdate();
                response.put("uploaded", true);
                return response;
        }
        response.put("uploaded", false);
        return response;
    }

    @RequestMapping(value = "/deleteAll", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public List<Article> deleteAll() {
        articleRepository.deleteAll();
        return articleRepository.findAll();
    }
}
