package com.pinyougou.api.controller;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Reference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.api.controller
 * @company www.itheima.com
 */
@RestController
@RequestMapping("/api")
public class ItemController {


    //private ResponseData{String message,Integer status,Object data}
    @Reference
    private ItemService itemService;

    @Autowired
    private RestTemplate restTemplate;
    //url
    //参数
    //返回值
    //method的类型
    @RequestMapping(value="/item/{id}",method = RequestMethod.GET)
    public ResponseEntity<TbItem> getItemById(@PathVariable  Long id){
//        restTemplate.post
        try {
            TbItem tbItem = itemService.findOne(id);
            return ResponseEntity.status(HttpStatus.OK).body(tbItem);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

}
