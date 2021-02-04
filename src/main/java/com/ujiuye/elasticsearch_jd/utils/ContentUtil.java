package com.ujiuye.elasticsearch_jd.utils;

import com.ujiuye.elasticsearch_jd.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
@Component
public class ContentUtil {

    public List<Content> parseHTML(String keyword){
        List<Content> list=new ArrayList<>();
        String url="https://search.jd.com/Search?keyword="+keyword;
        Document document = null;
        try {
            document = Jsoup.parse(new URL(url), 300000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取文档对象
        Element goodsList = document.getElementById("J_goodsList");
        //获取li元素
        Elements lis = goodsList.getElementsByTag("li");
        //获取li里面的数据
        for (Element li:lis) {
            //以图片为主的网站，图片都是使用懒加载，所以获取source-data-lazy-img属性的值才能获取到
            String img = li.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = li.getElementsByClass("p-price").eq(0).text();
            String name = li.getElementsByClass("p-name").eq(0).text();
            list.add(new Content(name,img,price));
        }
        return list;
    }

}
