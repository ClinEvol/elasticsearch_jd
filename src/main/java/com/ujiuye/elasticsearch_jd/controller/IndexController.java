package com.ujiuye.elasticsearch_jd.controller;

import com.ujiuye.elasticsearch_jd.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @RequestMapping("/index")
    public String index(){
        return "index";
    }

    @Autowired
    private ContentService contentService;

    @RequestMapping("/initData/{keyword}")
    @ResponseBody
    public String initData(@PathVariable("keyword") String keyword){
        try {
            contentService.parseHTMLAndInsertES(keyword);
            return keyword+"数据添加成功！";
        } catch (Exception e) {
            e.printStackTrace();
            return keyword+"数据添加失败！";
        }
    }

    @GetMapping("/search/{keyword}/{page}/{size}")
    @ResponseBody
    public List<Map<String, Object>> search(
            @PathVariable("keyword") String keyword,
            @PathVariable("page") int page,
            @PathVariable("size") int size) {
        return contentService.search(keyword,page,size);
    }
}
