package com.pinyougou.test;

import com.pinyougou.pojo.TbItem;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.test
 * @company www.itheima.com
 */
public class SolrJtest {


    @Test
    public void addIndex() throws Exception{
        //1.创建连接对象（solrserver）---httpsolrserver
        SolrServer solrServer = new HttpSolrServer("http://192.168.25.154:8080/solr");
        //2.创建文档solrinputdocument
        SolrInputDocument document = new SolrInputDocument();
        //3.添加域
        document.addField("id","test001");//id域
        document.addField("item_title","金光闪闪的手机");
        //4.添加到索引库中
        solrServer.add(document);
        //5.提交
        solrServer.commit();
    }


    @Test
    public void addIndexField() throws Exception{
        //1.创建连接对象（solrserver）---httpsolrserver
        SolrServer solrServer = new HttpSolrServer("http://192.168.25.154:8080/solr");
        TbItem item = new TbItem();
        item.setId(1992l);
        item.setTitle("你好");
        solrServer.addBean(item);
        solrServer.commit();
    }

    @Test
    public void testQuery() throws  Exception{
        //1.创建连接对象（solrserver）---httpsolrserver
        SolrServer solrServer = new HttpSolrServer("http://192.168.25.154:8080/solr");
        //2.创建查询的对象
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        //3.查询
        QueryResponse response = solrServer.query(query);
        //4.获取结果
        SolrDocumentList results = response.getResults();

        System.out.println("总记录数>>"+results.getNumFound());//总记录数

        for (SolrDocument result : results) {
            System.out.println(result.getFieldValue("item_title"));//---->封装到一个POJO中 显示在页面中
            TbItem tiem= new TbItem();
            tiem.setTitle((String)result.getFieldValue("item_title"));
        }
    }



    @Test
    public void testQueryField() throws  Exception{
        //1.创建连接对象（solrserver）---httpsolrserver
        SolrServer solrServer = new HttpSolrServer("http://192.168.25.154:8080/solr");
        //2.创建查询的对象
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        QueryResponse response = solrServer.query(query);
        List<TbItem> tbItems = response.getBeans(TbItem.class);
//
//        for (TbItem tbItem : tbItems) {
//            System.out.println(tbItem.getTitle());
//        }
    }
}
