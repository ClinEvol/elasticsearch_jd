package com.ujiuye.elasticsearch_jd.service;

import com.ujiuye.elasticsearch_jd.pojo.Content;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ContentService {
    /*
    抓取京东数据并添加到ES中
     */
    void parseHTMLAndInsertES(String keyword) throws IOException;

    List<Map<String, Object>> search(String keyword, int page, int size);
}
