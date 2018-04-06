package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.controller
 * @company www.itheima.com
 */
@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {

    @Reference(timeout = 3000)
    private ItemSearchService itemSearchService;

    @RequestMapping("/search")
    public Map search(@RequestBody  Map searchMap){
        Map resultMap = itemSearchService.search(searchMap);
        return resultMap;
    }
}
