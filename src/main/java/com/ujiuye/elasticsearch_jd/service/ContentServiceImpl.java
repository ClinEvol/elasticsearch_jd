package com.ujiuye.elasticsearch_jd.service;

import com.alibaba.fastjson.JSON;
import com.ujiuye.elasticsearch_jd.pojo.Content;
import com.ujiuye.elasticsearch_jd.utils.ContentUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentServiceImpl implements ContentService {
    @Autowired
    private ContentUtil contentUtil;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void parseHTMLAndInsertES(String keyword) throws IOException {

        //抓取到的京东数据
        List<Content> contents = contentUtil.parseHTML(keyword);
        //批量添加到ES中
        BulkRequest bulkRequest=new BulkRequest();
        bulkRequest.timeout("20s");
        for (Content content : contents) {
            bulkRequest.add(new IndexRequest("jd_index").source(JSON.toJSONString(content), XContentType.JSON));
        }
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public List<Map<String, Object>> search(String keyword,int page,int size) {
        //查询请求
        SearchRequest searchRequest=new SearchRequest("jd_index");
        //构建请求条件
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();
        sourceBuilder.highlighter();
        //设置需要精确查询的字段
        TermQueryBuilder termQuery = QueryBuilders.termQuery("name", keyword);
        sourceBuilder.query(termQuery);
        //设置分页
        sourceBuilder.from((page - 1) * size);
        sourceBuilder.size(size);
        //响应时间
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //高亮
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        highlightBuilder.field("name");//高亮的字段
        highlightBuilder.requireFieldMatch(false);////如果要多个字段高亮,这项要为false
        highlightBuilder.preTags("<span style='color:red;'>");//字段加前缀
        highlightBuilder.postTags("</span>");
        //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
        highlightBuilder.fragmentSize(800000); //最大高亮分片数
        highlightBuilder.numOfFragments(0); //从第一个分片获取高亮片段
        //把高亮关联到sourceBuilder
        sourceBuilder.highlighter(highlightBuilder);


        //把搜索条件关联到查询请求中
        searchRequest.source(sourceBuilder);
        //执行查询
        SearchResponse search = null;
        try {
            search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SearchHit[] hits = search.getHits().getHits();
        List<Map<String, Object>> list = new ArrayList<>();//保存处理后的内容

        //处理高亮的内容
        for (SearchHit searchHit :hits){
            //查询结果
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //获取高亮的字段内容 {name=[name], fragments[[陈<span style='color:red;'>向</span>南]]}
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            //把高亮的字段重新放到查询结果中
            HighlightField field = highlightFields.get("name");
            if(field!= null){
                Text[] fragments = field.fragments();
                String n_field = "";
                for (Text fragment : fragments) {
                    n_field += fragment;
                }
                //高亮标题覆盖原标题
                sourceAsMap.put("name",n_field);
            }
            list.add(searchHit.getSourceAsMap());
        }
        return list;
    }
}
