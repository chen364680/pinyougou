package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
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
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        //通过redis 获取  该登录的用户的支付日志记录   将记录中的生成的支付订单号 以及要支付的金额
        //写一个服务层代码，直接调用
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog= weixinPayService.getPayLogFromRedis(userId);
        return  weixinPayService.createNative(payLog.getOutTradeNo()+"",payLog.getTotalFee()+"");//单位是分
    }

    @RequestMapping("queryStatus")
    public Result queryStatus(String out_trade_no){
        Result result = null;
        //超时时间如果5分钟，重新生成二维码

        int count = 0;
        while (true){
            Map map = weixinPayService.queryStatus(out_trade_no);
            if(map==null){
               return new Result(false,"支付出错");
            }

            if(map.get("trade_state").equals("SUCCESS")){
                //如果支付成功，需要 更新订单表的状态为2（已付款）

                   //通过out_trade_no查询支付记录     获取到 订单列表"37,38"
                   //通过传递订单ID 去获取订单对象   将该对象的状态修改即可。
                orderService.updateOrderStatus(out_trade_no);
                //要更新 支付日志表的 支付时间和支付的状态1（已支付）
                orderService.updatePayLogStatus(out_trade_no,(String) map.get("transaction_id"));




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
                break;
            }


        }
        return result;
    }
}
