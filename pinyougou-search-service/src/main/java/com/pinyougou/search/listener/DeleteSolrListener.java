package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;
import java.io.Serializable;
import java.util.Arrays;
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
public class DeleteSolrListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        //删除索引库
        //1.接收消息
        if(message instanceof ObjectMessage){
            try {
                ObjectMessage message1 = (ObjectMessage) message;
                Long[] ids = (Long[]) message1.getObject();
               itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }


    }
}
