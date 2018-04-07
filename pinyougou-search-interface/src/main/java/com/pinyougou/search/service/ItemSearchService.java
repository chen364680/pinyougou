package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.service
 * @company www.itheima.com
 */
public interface ItemSearchService {
    /**
     * 根据传递过来的参数从索引库中搜索数据
     * @param searchMap
     * @return
     */
    public Map search(Map searchMap);

    //导入索引库数据
    public void importItemListData(List itemList);

    public void deleteByGoodsIds(List goodsIdList);
}
