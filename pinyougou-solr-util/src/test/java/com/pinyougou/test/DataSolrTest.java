package com.pinyougou.test;

import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.test
 * @company www.itheima.com
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:spring/*.xml")
public class DataSolrTest {


    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void testDataSolrAdd(){
        //需要用到@Field注解
        TbItem item = new TbItem();
        item.setId(1L);
        item.setBrand("华为");
        item.setCategory("手机");
        item.setGoodsId(1L);
        item.setSeller("华为2号专卖店");
        item.setTitle("华为Mate9");
        item.setPrice(new BigDecimal(2000));
        solrTemplate.saveBean(item);
        solrTemplate.commit();
    }

    @Test
    public void queryById(){
        TbItem item = solrTemplate.getById("1", TbItem.class);//相当于  查询 得到 document的对象 ----》转成TBITEM
        System.out.println(item.getTitle());
    }

    @Test
    public void testDelete(){
        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }


    //构建数据

    @Test
    public void addList(){
        List<TbItem> items = new ArrayList<>();
        for (long i=1;i<100;i++) {
            TbItem item = new TbItem();
            item.setId(i);
            item.setBrand("华为"+i);
            item.setCategory("手机"+i);
            item.setGoodsId(1L);
            item.setSeller("华为2号专卖店"+i);
            item.setTitle("华为Mate"+i);
            item.setPrice(new BigDecimal(2000));
            items.add(item);
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

    @Test
    public void pageQuery(){
        Query query = new SimpleQuery("*:*");
        query.setOffset(40);//(page-1 * rows)
        query.setRows(20);//rows
        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);
        List<TbItem> content = items.getContent();
        System.out.println("总记录数："+items.getTotalElements());
        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }
    }


    @Test
    public void CrietiearQuery(){
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_title");//item_title:shouji
        criteria.contains("2").and("item_title").is("华为");//item_title:2 AND item_title:华为
//        criteria.is("华为");
        query.addCriteria(criteria);
        System.out.println(criteria);
        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);
        List<TbItem> content = items.getContent();
        System.out.println("总记录数："+items.getTotalElements());
        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }
    }



}
