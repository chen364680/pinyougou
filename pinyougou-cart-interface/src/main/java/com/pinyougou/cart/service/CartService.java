package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.cart.service
 * @company www.itheima.com
 */
public interface CartService {
    /**
     * 向已有的购物列表中 添加商品
     * @param cartList 已有的购物车列表
     * @param itemId 要添加的商品的ID
     * @param num 要添加数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    //从redis中获取购物车的列表
    public List<Cart> getCartListFromRedis(String username);
    //向redis中存储购物车的列表
    public void saveCartListToRedis(String username,List<Cart> cartList);

    public List<Cart> mergeCartList(List<Cart> cookieList,List<Cart> redisList);
}
