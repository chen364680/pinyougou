package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.cart.service.impl
 * @company www.itheima.com
 */
@Service(timeout = 6000)
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
            //1.根据商品的ID 查询商品的信息 item
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //2. 先获取要添加的商品的商家民（sellerId）
        String sellerId = item.getSellerId();
        Cart cart =   searchCartBySellerId(sellerId,cartList);
        if(cart!=null){ //3.判断商家是否在已有的购物车列表中存在
            //3.1 如果存在
              // 判断要添加的商品是否在明细列表中存在
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem orderItem = searchOrderItemByItemId(itemId,orderItemList);
            if(orderItem!=null) {
                //如果存在    数量相加  金额更新
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));

                //判断如果减没了 删除这个记录
                if(orderItem.getNum()<=0){
                    orderItemList.remove(orderItem);
                }

                //如果整个明细列表都没数据 删除购物车对象
                if(orderItemList.size()==0){
                    cartList.remove(cart);
                }

            }else {
                //如果不存在  向明细列表中添加一个商品
                orderItem = new TbOrderItem();
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
                orderItem.setNum(num);
                orderItem.setGoodsId(item.getGoodsId());
                orderItem.setItemId(itemId);
                orderItem.setPicPath(item.getImage());
                orderItem.setPrice(item.getPrice());
                orderItem.setTitle(item.getTitle());
                orderItem.setSellerId(sellerId);
                orderItemList.add(orderItem);
            }
        }else{
            //3.2 如果不存在  要添加的商品 所属的商家 还没有在购物车列表中存在
            //构建一个信息的cart对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());//店铺
            //构建明细列表  向明细列表中加一个商品
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = new TbOrderItem();
            orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
            orderItem.setNum(num);
            orderItem.setGoodsId(item.getGoodsId());
            orderItem.setItemId(itemId);
            orderItem.setPicPath(item.getImage());
            orderItem.setPrice(item.getPrice());
            orderItem.setTitle(item.getTitle());
            orderItem.setSellerId(sellerId);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> getCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);

        if(cartList==null){
            return new ArrayList<>();
        }

        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cookieList, List<Cart> redisList) {
        //合并

        for (Cart cart : cookieList) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {//要添加到redis中的商品
                redisList= addGoodsToCartList(redisList,orderItem.getItemId(),orderItem.getNum());//向已有的购物车列表中添加一个商品（。。。。。逻辑）
            }
        }
        return redisList;
    }

    private TbOrderItem searchOrderItemByItemId(Long itemId, List<TbOrderItem> orderItemList) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 判断在哟有的购物车列表中获取购物车对象（商家）
     * @param sellerId
     * @param cartList
     * @return
     */
    private Cart searchCartBySellerId(String sellerId, List<Cart> cartList) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
        
    }
}
