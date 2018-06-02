package com.pinyougou.seckill.service;
import java.util.List;
import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

	/**
	 * 秒杀下订单
	 * @param seckillId 商品的ID
	 * @param userId  下单的用户
	 */
	public void submitOrder(Long seckillId,String userId);

	/**
	 * 从nosql数据库 redis中获取该用户的订单信息
	 * @param userId
	 * @return
	 */
	public TbSeckillOrder searchSeckillOrderByUserId(String userId);


	/**
	 *
	 * @param userId 用户Id
	 * @param transanctionId
	 * @param orderId  传递过来的订单号（支付的订单号 要和通过用户查询到的订单中的订单号做比较）
	 */
	public void  saveSeckillOrderFromRedisTobd(String userId,String transanctionId,Long orderId);

	/**
	 * 删除原来的订单 以及库存增加
	 * @param userId
	 * @param orderId
	 */
	public void deleteOrderFromRedis(String userId,Long orderId);
	
}
