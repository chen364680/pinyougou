package com.pinyougou.page.service;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.page.service
 * @company www.itheima.com
 */
public interface ItemPageService {
    /**
     * 根据商品SPU的id 查询商品的信息（基本信息和描述信息） 调用生成HTML的API(freemarker)
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);
    public boolean deleteHtml(Long[] goodsId);
}
