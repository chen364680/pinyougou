package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private IdWorker idWorker;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private TbPayLogMapper payLogMapper;


	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//生成全局唯一的订单的Id 通过雪花算法来实现.

		//1.生成唯一的订单的ID


		//2.从redis中将购物车列表数据获取到List<cart>  cart:sellerId
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		double total_fee=0;

		List<String> orderIds = new ArrayList<>();
		for (Cart cart : cartList) {
			//3.构建订单对象
			long orderId = idWorker.nextId();
			//商家的ID
			String sellerId = cart.getSellerId();
			TbOrder tbOrder = new TbOrder();
			tbOrder.setSourceType(order.getSourceType());
			tbOrder.setUserId(order.getUserId());
//			tbOrder.setPayment(order.getPayment());//所有的商家的总金额
			double money =0.00d;
			tbOrder.setReceiver(order.getReceiver());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(tbOrder.getCreateTime());
			tbOrder.setStatus("1");//1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
			tbOrder.setPaymentType(order.getPaymentType());
			tbOrder.setOrderId(orderId);
			tbOrder.setPostFee("0");
			tbOrder.setSellerId(sellerId);//订单只属于这个商家的


			for(TbOrderItem orderItem :cart.getOrderItemList()){
				money+= orderItem.getTotalFee().doubleValue();//每一个商品 （购买的数量*价格）    money:是一个订单的总金额
				//添加订单的明细
				orderItem.setOrderId(orderId);
				long orderItemId = idWorker.nextId();
				orderItem.setId(orderItemId);
				orderItemMapper.insert(orderItem);

			}
			total_fee+=money;

			tbOrder.setPayment(new BigDecimal(money));
			orderIds.add(orderId+"");
			orderMapper.insert(tbOrder);

			redisTemplate.boundHashOps("cartList").delete(order.getUserId());
		}

		if("1".equals(order.getPaymentType()))//是微信支付
		{
			//插入支付记录  状态一定是未支付的状态 判断 是微信支付的时候做。
			TbPayLog payLog = new TbPayLog();
			payLog.setUserId(order.getUserId());
			payLog.setTradeState("0");//未支付
			payLog.setTotalFee((long)(total_fee*100));//金额 分
			payLog.setPayType("1");//微信支付
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单的ID
			//orderIds.toString()//[1,2,3,4]
			payLog.setOrderList(orderIds.toString().replace("[","").replace("]",""));
			payLog.setCreateTime(new Date());
			payLogMapper.insert(payLog);
			//使用缓存 将 用户对应的支付订单的记录  存在redis中  key:payLog   field :userId  value:payLog
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
		}




		//.保存订单
//		orderMapper.insert(order);

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	//只是更新订单的状态
	@Override
	public void updateOrderStatus(String out_trade_no) {
		//根据支付日志表的主键查询 记录
		TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		//获取订单的列表字符串
		String orderList = tbPayLog.getOrderList();
		String[] split = orderList.split(",");

		//更新状态
		for (String orderId : split) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
			//更新
			tbOrder.setStatus("2");//已付款
			tbOrder.setPaymentTime(new Date());//支付时间
			tbOrder.setUpdateTime(tbOrder.getPaymentTime());//更新记录的时间
			orderMapper.updateByPrimaryKey(tbOrder);
			redisTemplate.boundHashOps("payLog").delete(tbPayLog.getUserId());
		}
	}

	@Override
	public void updatePayLogStatus(String out_trade_no, String transantionId) {
		TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		tbPayLog.setTradeState("1");//已支付
		tbPayLog.setPayTime(new Date());
		tbPayLog.setTransactionId(transantionId);
		payLogMapper.updateByPrimaryKey(tbPayLog);
		redisTemplate.boundHashOps("payLog").delete(tbPayLog.getUserId());
	}

}
