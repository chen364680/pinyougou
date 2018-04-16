package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtil;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.cart.controller
 * @company www.itheima.com
 */
@RequestMapping("/cart")
@RestController
public class CartController {


    @Reference(timeout = 6000)
    private CartService cartService;

    //获取购物车的列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(">>>"+name);
        if("anonymousUser".equals(name)) {
            //如果用户没登录 ，操作cookie
            String cartListString = CookieUtil.getCookieValue(request, "cartList", true);//设置cookie是要转码 获取数据是要解码
            List<Cart> carts=new ArrayList<>();
            if(StringUtils.isNotBlank(cartListString)) {
                carts = JSON.parseArray(cartListString, Cart.class);
            }
            return carts;
        }else {
            //如果用户已登录，操作redis
            List<Cart> cartListFromRedis = cartService.getCartListFromRedis(name);
            //和并
            String cartListString = CookieUtil.getCookieValue(request, "cartList", true);//设置cookie是要转码 获取数据是要解码
            List<Cart> carts=new ArrayList<>();
            if(StringUtils.isNotBlank(cartListString)) {
                carts = JSON.parseArray(cartListString, Cart.class);
            }
            if(carts.size()==0){
                return cartListFromRedis;
            }
            List<Cart> cartsNew = cartService.mergeCartList(carts, cartListFromRedis);//返回的是最新的购物车列表
            cartService.saveCartListToRedis(name,cartsNew);
            //删除cookie
            CookieUtil.deleteCookie(request,response,"cartList");

           cartListFromRedis = cartService.getCartListFromRedis(name);
            return cartListFromRedis;
        }
    }

    //添加购物车
    //CrossOrigin
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//统一指定的域访问我的服务器资源
//        response.setHeader("Access-Control-Allow-Credentials", "true");//同意客户端携带cookie

        //如果用户没登录 ，操作cookie
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if("anonymousUser".equals(name)) {
            //向已有的购物车（[]）中添加一个商品  逻辑放在service :向已有的购物车中添加商品
            //先从cookie中获取购物车列表
            String cartListString = CookieUtil.getCookieValue(request, "cartList", true);//设置cookie是要转码 获取数据是要解码
            List<Cart> carts = new ArrayList<>();
            if (StringUtils.isNotBlank(cartListString)) {
                carts = JSON.parseArray(cartListString, Cart.class);
            }
            List<Cart> cartsnew = cartService.addGoodsToCartList(carts, itemId, num);
            //设置回cookie中
            CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartsnew), 24 * 3600, true);
        }else {
            //如果用户已登录，操作redis
            List<Cart> cartListFromRedis = cartService.getCartListFromRedis(name);
            List<Cart> cartsNew = cartService.addGoodsToCartList(cartListFromRedis, itemId, num);
            cartService.saveCartListToRedis(name,cartsNew);
        }
        return new Result(true,"添加成功");
    }

}
