package com.pinyougou.pay.service;

import com.pinyougou.pojo.TbPayLog;

import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.pay.service
 * @company www.itheima.com
 */
public interface WeixinPayService {

    //发送请求到微信端 调用统一下单的接口 返回要支付的二维码连接
    public Map createNative(String out_trade_no,String total_fee);

    /**
     * 根据支付订单号 查询该支付订单的支付状态
     * @param out_trade_no
     * @return
     */
    public Map queryStatus(String out_trade_no);

    public TbPayLog getPayLogFromRedis(String userId);
}
