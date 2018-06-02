package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;
import com.pinyougou.seckill.service.SeckillGoodsService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillGoods> page=   (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillGoods!=null){			
						if(seckillGoods.getTitle()!=null && seckillGoods.getTitle().length()>0){
				criteria.andTitleLike("%"+seckillGoods.getTitle()+"%");
			}
			if(seckillGoods.getSmallPic()!=null && seckillGoods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+seckillGoods.getSmallPic()+"%");
			}
			if(seckillGoods.getSellerId()!=null && seckillGoods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillGoods.getSellerId()+"%");
			}
			if(seckillGoods.getStatus()!=null && seckillGoods.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillGoods.getStatus()+"%");
			}
			if(seckillGoods.getIntroduction()!=null && seckillGoods.getIntroduction().length()>0){
				criteria.andIntroductionLike("%"+seckillGoods.getIntroduction()+"%");
			}
	
		}
		
		Page<TbSeckillGoods> page= (Page<TbSeckillGoods>)seckillGoodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
    @Override
    public List<TbSeckillGoods> findList() {
		//先从redis中查询 ，如果存在 那么直接返回列表
		try {
			List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();

			if(seckillGoods!=null && seckillGoods.size()>0){
                return seckillGoods;
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

		Date date = new Date();
		//1.创建example 添加查询的条件
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//已经审核过的商品
		criteria.andStockCountGreaterThan(0);//商品的库存要大于0
		criteria.andStartTimeLessThanOrEqualTo(date);//开始时间要小于等于当前时间
		criteria.andEndTimeGreaterThan(date);//结束时间要大于当前的时间
		//2.执行
		List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample(example);
		
		//将列表 存入到redis中
		try {
			for (TbSeckillGoods tbSeckillGood : tbSeckillGoods) {
                redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGood.getId(),tbSeckillGood);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tbSeckillGoods;
    }

	@Override
	public TbSeckillGoods findOneFromRedis(Long seckillId) {
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		return seckillGoods;
	}

}
