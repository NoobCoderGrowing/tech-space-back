package com.example.techspace.controller;


import com.alibaba.fastjson2.JSONObject;
import com.example.techspace.ArticleRepository;
import com.example.techspace.entity.Article;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/public")
@EnableMethodSecurity
public class PublicContent {

    @Resource
    ArticleRepository articleRepository;

    @RequestMapping(value = "/retrieve/articles", method = RequestMethod.GET)
    @ResponseBody
    public List<Article> retrieveArticles(){
        List<Article> articles = articleRepository.findAll();
        return articles;
    }

    @RequestMapping(value = "/retrieve/test", method = RequestMethod.GET)
    @ResponseBody
    public HashMap<String, String> test(){
        Article article = new Article();
        article.setTitle("good");
        article.setDate(new Date().toString());
        article.setContent("hhhhhhh");
        articleRepository.save(article);
        List<Article> result = articleRepository.findAll();
        HashMap<String, String> response = new HashMap<String, String>();
        response.put("success", JSONObject.toJSONString(result));
        return response;
    }
}
