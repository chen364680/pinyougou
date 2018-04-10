package com.pinyougou.page.listener;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.page.listener
 * @company www.itheima.com
 */
public class GeneraterHtmlListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        if(message instanceof ObjectMessage){
            //1.接收消息  long[]
            try {
                ObjectMessage message1 = (ObjectMessage) message;
                Long[] ids = (Long[])message1.getObject();
                for (Long id : ids) {
                    //2.循环遍历 数组  调用生成静态页面的方法即可
                    itemPageService.genItemHtml(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
