package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**做登录相关的操作  比如：获取登录的用户名
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.manager.controller
 * @company www.itheima.com
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    //获取当前登录的用户的用户名
    @RequestMapping("getLoginName")
    public Map getLoginUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName",username);
        return map;
    }
}
