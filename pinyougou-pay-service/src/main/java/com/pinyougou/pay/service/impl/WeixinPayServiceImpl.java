package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.pay.service.impl
 * @company www.itheima.com
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Value("${notifyurl}")
    private String notifyurl;

    /**
     * @param out_trade_no
     * @param total_fee    金额(分)
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.传递必要的参数列表  封装起来放在map中
        Map param = new HashMap();
        param.put("appid", appid);//公众号ID
        param.put("mch_id", partner);//商家ID
        param.put("nonce_str", WXPayUtil.generateNonceStr());//设置随机字符串
        //设置签名 通过工具类产生一个有签名的XML
        param.put("body", "品优购");//商品描述
        param.put("out_trade_no", out_trade_no);
        param.put("total_fee", total_fee);
        param.put("spbill_create_ip", "127.0.0.1");
        param.put("notify_url", notifyurl);
        param.put("trade_type", "NATIVE");
        //2.生成一个带签名的xml
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //3.发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);//发送请求的时候携带的数据
            httpClient.post();//发送post请求 并执行
            //4.获取结果
            String resultXml = httpClient.getContent();
            Map<String, String> xmlMap = WXPayUtil.xmlToMap(resultXml);

            //5.封装结果 （包括二维码连接URL）
            Map resultMap = new HashMap();
            resultMap.put("code_url",xmlMap.get("code_url"));
            resultMap.put("out_trade_no",out_trade_no);
            resultMap.put("total_fee",total_fee);
            return resultMap;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }


    }

    @Override
    public Map queryStatus(String out_trade_no) {
        //1.传递必要的参数列表  封装起来放在map中
        Map param = new HashMap();
        param.put("appid", appid);//公众号ID
        param.put("mch_id", partner);//商家ID
        param.put("nonce_str", WXPayUtil.generateNonceStr());//设置随机字符串
        //设置签名 通过工具类产生一个有签名的XML
        param.put("out_trade_no", out_trade_no);

        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //3.发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);//发送请求的时候携带的数据
            httpClient.post();//发送post请求 并执行
            //4.获取结果
            String resultXml = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            System.out.println(resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public TbPayLog getPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }
}
