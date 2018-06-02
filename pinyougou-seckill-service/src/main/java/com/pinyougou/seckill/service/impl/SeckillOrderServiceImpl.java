package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper tbSeckillGoodsMapper;


    @Override
    public void submitOrder(Long seckillId, String userId) {
        //1.根据商品的ID 从redis查询商品的数据
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if(seckillGoods==null ){
			//判断 商品不存在
			throw new RuntimeException("商品不存在");
		}

		if(seckillGoods.getStockCount()<=0){
			//判断 商品剩余库存是否已经为0
			throw new RuntimeException("商品已经被抢光了");
		}
		//2.创建订单
			//先 库存-1
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
		//库存减少要更新数据库
		redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);
		if(seckillGoods.getStockCount()==0){
			//判断 商品的剩余库存是否已经为0  如果为0   更新到数据库中，并且将这个商品在redis中删除。
			tbSeckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
		}
		//在redis中创建订单 （状态 是 未支付 ）
		TbSeckillOrder order = new TbSeckillOrder();
		order.setCreateTime(new Date());
		order.setId(new IdWorker(0,1).nextId());//秒杀商品的ID
		order.setMoney(seckillGoods.getCostPrice());//价格
		order.setSeckillId(seckillId);//商品的ID
		order.setSellerId(seckillGoods.getSellerId());//商品所属的商家的ID
		order.setStatus("0");//未支付
		order.setUserId(userId);
		//保存
		redisTemplate.boundHashOps("seckillOrder").put(userId,order);//用户的订单
    }

	@Override
	public TbSeckillOrder searchSeckillOrderByUserId(String userId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);//用户的订单
		return seckillOrder;
	}

	@Override
	public void saveSeckillOrderFromRedisTobd(String userId, String transanctionId, Long orderId) {
			//1.根据用户的ID从redis中获取订单信息对象
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder==null ){
			throw new RuntimeException("订单不存在");
		}
		if(seckillOrder.getId()!=orderId.longValue()){
			throw new RuntimeException("订单号不一致");
		}
		//2. 更新状态 支付时间  流水号
		seckillOrder.setStatus("1");
		seckillOrder.setPayTime(new Date());//更新支付时间
		seckillOrder.setTransactionId(transanctionId);//支付流水


		//3.保存到数据库中
		seckillOrderMapper.insert(seckillOrder);

		//4.删除redis原来的订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);


	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder==null ){
			throw new RuntimeException("订单不存在");
		}

		if(seckillOrder.getId()!=orderId.longValue()){
			throw new RuntimeException("订单号不一致");
		}


		//库存增加
		//先获取到商品 如果原来的商品 不存在呢？

		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
		if(seckillGoods!=null) {
			seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getSellerId(), seckillGoods);
		}else{
			//说明原来是只有一个库存的商品
			//1.从数据库中获取商品的数据  并且 将库存设置为1
			//2.设置数据库中的商品的库存为1
			System.out.println("afsafdsa");
		}
		//删除原来的订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);

	}

}
