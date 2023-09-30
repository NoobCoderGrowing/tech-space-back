package com.example.techspace.controller;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.techspace.ArticleRepository;
import com.example.techspace.entity.Article;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/admin")
@EnableMethodSecurity
public class ContentMangement {

    @Resource
    ArticleRepository articleRepository;

    @RequestMapping(value = "/upload/article", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public HashMap<String, Boolean> uploadArticle(@RequestBody Map<String,String> submitArticle){
        String params = JSONObject.toJSONString(submitArticle);
        System.out.println(params);
        Article article = JSON.parseObject(params, Article.class);
        articleRepository.save(article);
        HashMap<String, Boolean> response = new HashMap<String, Boolean>();
        response.put("uploaded", true);
        return response;
    }
}
