package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.cart.controller
 * @company www.itheima.com
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;//秒杀订单的服务


    /**
     * 构建秒杀订单的支付二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //通过redis 获取  该登录的用户的支付日志记录   将记录中的生成的支付订单号 以及要支付的金额
        //写一个服务层代码，直接调用
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
//        TbPayLog payLog= weixinPayService.getPayLogFromRedis(userId);

        //从redis中获取该用户的秒杀订单的订单信息（包括订单号，包括要付款的金额）
        TbSeckillOrder order = seckillOrderService.searchSeckillOrderByUserId(userId);
        double v = order.getMoney().doubleValue() * 100;
        long fen = (long)v;
        return  weixinPayService.createNative(order.getId()+"",fen+"");//单位是分
    }

    @RequestMapping("queryStatus")
    public Result queryStatus(String out_trade_no){
        Result result = null;
        //超时时间如果5分钟，重新生成二维码
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        int count = 0;
        while (true){
            Map map = weixinPayService.queryStatus(out_trade_no);
            if(map==null){
               return new Result(false,"支付出错");
            }

            if(map.get("trade_state").equals("SUCCESS")){
                //如果支付成功，需要 更新订单表的状态为2（已付款）
                //支付成功：第一：需要创建订单到数据库中  第二：更新状态（已支付 支付的时间，交易流水）  第三：删除原来的redis中的该用户的订单。
                seckillOrderService.saveSeckillOrderFromRedisTobd(userId,(String)map.get("transaction_id"),Long.valueOf(out_trade_no));
                return new Result(true,"支付成功");
            }
            //隔一段时间 3秒钟
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;

            if(count>=100){
                result = new Result(false,"支付超时");
                //超时 需要取消订单
               //1. 关闭微信的支付订单，
                Map map1 = weixinPayService.closePay(out_trade_no);
                if("ORDERPAID".equals(map1.get("err_code"))){
                    //订单已经支付
                    seckillOrderService.saveSeckillOrderFromRedisTobd(userId,(String)map.get("transaction_id"),Long.valueOf(out_trade_no));
                    return new Result(true,"支付成功");
                }else {
                    // 2.并删除nosql数据库中的订单，并且将库存增加。
                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                }
                result= new Result(false,"超时了，取消订单");
                break;
            }


        }
        return result;
    }
}
