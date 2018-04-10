package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.page.service.impl
 * @company www.itheima.com
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pageDir}")
    private String pageDir;

    @Autowired
    private FreeMarkerConfigurer configurer;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public boolean genItemHtml(Long goodsId) {
        //模板 + 数据集= html
        FileWriter out=null;
        try {
            //1.创建configuration对象
            Configuration configuration = configurer.getConfiguration();
            //2.设置模板所在的位置 设置字符编码  spring管理了
            //3.创建模板文件  获取模板对象
            Template template = configuration.getTemplate("item.ftl");

            //4.获取数据集
            Map  model = new HashMap();
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //查询商品SPU的一级二级三级分类的名称
            TbItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id());
            TbItemCat itemCat2 =itemCatMapper.selectByPrimaryKey(goods.getCategory2Id());
            TbItemCat itemCat3 =itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());

            //将SPU的所有对应的SKU的列表查询出来展示到页面中
            TbItemExample exmaple = new TbItemExample();
            TbItemExample.Criteria criteria = exmaple.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");
            exmaple.setOrderByClause("is_default desc");//降序排列  查询到的第一个就是要默认展示的SKU
            List<TbItem> skuList = itemMapper.selectByExample(exmaple);

            model.put("goods",goods);
            model.put("goodsDesc",goodsDesc);
            model.put("itemCat1",itemCat1.getName());
            model.put("itemCat2",itemCat2.getName());
            model.put("itemCat3",itemCat3.getName());
            model.put("skuList",skuList);


            //5.输出文件
            out = new FileWriter(pageDir+goodsId+".html");
            template.process(model,out);


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
                if(out!=null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public boolean deleteHtml(Long[] goodsId) {

        try {
            for (Long id : goodsId) {
                File file = new File(pageDir+id+".html");
                if(file.exists()){
                    file.delete();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
