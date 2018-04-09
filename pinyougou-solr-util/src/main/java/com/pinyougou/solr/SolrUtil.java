package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.solr
 * @company www.itheima.com
 */
public class SolrUtil {

    @Autowired
    private SolrTemplate solrTemplate;


    @Autowired
    private TbItemMapper tbItemMapper;

    public void importDataToIndex(){
        //1.获取mapper的对象
        TbItemExample example = new TbItemExample();

        example.createCriteria().andStatusEqualTo("1");//将正常的商品数据导入

        //SPU  aut_stats=1 才导入

        List<TbItem> tbItems = tbItemMapper.selectByExample(example);
        for (TbItem tbItem : tbItems) {
            String spec = tbItem.getSpec();//string类型的规格与规格选项
            if(spec!=null && spec.length()>0) {
                Map specMap = JSON.parseObject(spec, Map.class);
                tbItem.setSpecMap(specMap);
            }
        }

        //2.查询所有的数据库的商品的数据
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
        //3.数据添加到索引库中
    }


    public static void main(String[] args) {
        //初始化spring容器 获取solrtemplate对象
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
        SolrUtil solrUtil =(SolrUtil) context.getBean("solrUtil");
        //调用从数据库查询数据导入到索引库的方法
        solrUtil.importDataToIndex();
    }
}
