package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.listener
 * @company www.itheima.com
 */
public class UpdateSolrListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        //更新索引库
        //1.接收消息
        if(message instanceof TextMessage){
            try {
                TextMessage message1 = (TextMessage) message;
                String stringjson = message1.getText();//是一个json list<tbitem>
                List<TbItem> tbItems = JSON.parseArray(stringjson, TbItem.class);
                //2.转成对象
                for (TbItem tbItem : tbItems) {
                    //转换规格数据
                    tbItem.setSpecMap(JSON.parseObject(tbItem.getSpec(),Map.class));
                }
                //3.更新索引库的动作（调用服务的方法）
                itemSearchService.importItemListData(tbItems);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }


    }
}
