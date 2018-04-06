package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.service.impl
 * @company www.itheima.com
 */
@Service(timeout = 5000)//默认的超时时间为1秒  timeout中的单位是毫秒
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Override
    public Map search(Map searchMap) {
        Map resultMap = new HashMap();
        Map map = searchList(searchMap);//高亮显示的值
        resultMap.putAll(map);//复制map中的所有的值放到resultmap

        //分组查询 商品的分类列表
        List categoryList = searchCategoryListByGroup(searchMap);//[手机，平板电视]
        resultMap.put("categoryList",categoryList);
        //如果页面传递了一个商品分类 ，那么应该查询的是这个商品分类下的品牌
        if(!searchMap.get("category").equals("")) {
            Map specAndBrandMap=  findBrandListAndSpecList((String)searchMap.get("category"));//获取第一个数据
            System.out.println("查询有数据的category"+searchMap.get("category"));
            resultMap.putAll(specAndBrandMap);
        }else{//如果商品分类没有被点击  默认查询第一个商品分类的品牌列表和规格列表
            //品牌和规格列表的查询
            if(categoryList!=null && categoryList.size()>0) {
                Map specAndBrandMap=  findBrandListAndSpecList((String) categoryList.get(0));//获取第一个数据
                resultMap.putAll(specAndBrandMap);
            }
        }

        //查询
        return resultMap;
    }


    /**
     * 根据查询的各种条件查询
     * @return
     */
    private Map searchList(Map searchMap){
        Map result = new HashMap();

        //1.根据关键字搜索 添加条件


        //2.设置高亮条件
        HighlightQuery highlightQuery = new SimpleHighlightQuery();
        Criteria criteria = new Criteria("item_keywords");
        criteria.is(searchMap.get("keywords"));//keywords代表的是从页面传递过来的额主查询的文本
        highlightQuery.addCriteria(criteria);


        HighlightOptions hightoptions = new HighlightOptions();

        hightoptions.addField("item_title");//高亮显示的域
        hightoptions.setSimplePrefix("<em style=\"color:red\">");
        hightoptions.setSimplePostfix("</em>");

        highlightQuery.setHighlightOptions(hightoptions);



        //2.2 添加过滤条件 （商品分类的过滤）
        if(!searchMap.get("category").equals("")) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteriacategory = new Criteria("item_category");//AND item_category:手机
            criteriacategory.is(searchMap.get("category"));
            System.out.println("category:"+criteriacategory);
            filterQuery.addCriteria(criteriacategory);
            highlightQuery.addFilterQuery(filterQuery);
        }


        //2.3 添加过滤条件 （品牌过滤）
        if(!searchMap.get("brand").equals("")) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria brandcriteria = new Criteria("item_brand");//AND item_brand:华为
            brandcriteria.is(searchMap.get("brand"));
            System.out.println("brandcriteria:"+brandcriteria);
            filterQuery.addCriteria(brandcriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }


        //2.4 添加过滤条件 （规格过滤）
        if(searchMap.get("spec")!=null) {
            Map<String,String> specMap = (Map) searchMap.get("spec");
            for (String key : specMap.keySet()) {//key就是：机身内存  网络
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria brandcriteria = new Criteria("item_spec_"+key);//AND item_spec_网络:移动3G
                brandcriteria.is(specMap.get(key));
                System.out.println("guige:"+brandcriteria);
                filterQuery.addCriteria(brandcriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }



        //查询
        HighlightPage<TbItem> hightPage = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);

        List<HighlightEntry<TbItem>> highlighted = hightPage.getHighlighted();
        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {
            TbItem entity = tbItemHighlightEntry.getEntity();//文档对象转成的POJO  也是不带高亮的
            List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();//获取高亮部分的数据
            if(highlights!=null && highlights.size()>0) {
                String highlightTitle = highlights.get(0).getSnipplets().get(0);//获取到的就是第一个域的第一个值
                entity.setTitle(highlightTitle);
            }

        }
        List<TbItem> content = hightPage.getContent();//获取到的是原来的不带高亮的数据   经过上边的设置就会有高亮的值了

        System.out.println("查询的总结果：》》》》"+hightPage.getTotalElements());
        result.put("rows",content);
        result.put("total",hightPage.getTotalElements());
        return result;
    }



    //分组查询

    private List searchCategoryListByGroup(Map searchMap){
        List<String> categoryList = new ArrayList<>();

        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords");
        criteria.is(searchMap.get("keywords"));//keywords代表的是从页面传递过来的额主查询的文本
        query.addCriteria(criteria);

        //分组查询的条件构建
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");//group by category
        query.setGroupOptions(groupOptions);

        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> itemCategory = groupPage.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = itemCategory.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();//获取分组后的结果

        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            String groupValue = tbItemGroupEntry.getGroupValue();
            categoryList.add(groupValue);
        }
        return categoryList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private Map findBrandListAndSpecList(String categoryName){
        Map result = new HashMap();
        //从缓存中获取数据
        Long typeId = Long.valueOf((Integer)redisTemplate.boundHashOps("itemCat").get(categoryName));

        //获取品牌列表和 规格列表
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        result.put("brandList",brandList);
        result.put("specList",specList);
        return result;
    }
}
