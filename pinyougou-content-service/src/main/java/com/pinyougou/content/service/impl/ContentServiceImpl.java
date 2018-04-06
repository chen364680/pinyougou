package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import entity.PageResult;
import entity.PinyougouConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.LinkedList;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		try {
			redisTemplate.boundHashOps(PinyougouConstants.SPRING_DATA_REDIS_HASH_KEY_CONTENT).delete(content.getCategoryId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		contentMapper.insert(content);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		try {
			TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());//查询原来的数据库中的广告

			Long categoryId = tbContent.getCategoryId();//之前的categoryId
			redisTemplate.boundHashOps(PinyougouConstants.SPRING_DATA_REDIS_HASH_KEY_CONTENT).delete(categoryId);

			if(categoryId!=content.getCategoryId()) {
				redisTemplate.boundHashOps(PinyougouConstants.SPRING_DATA_REDIS_HASH_KEY_CONTENT).delete(content.getCategoryId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		contentMapper.updateByPrimaryKey(content);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			try {
				TbContent content = contentMapper.selectByPrimaryKey(id);
				redisTemplate.boundHashOps(PinyougouConstants.SPRING_DATA_REDIS_HASH_KEY_CONTENT).delete(content.getCategoryId());
			} catch (Exception e) {
				e.printStackTrace();
			}
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getContent()!=null && content.getContent().length()>0){
				criteria.andContentLike("%"+content.getContent()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private RedisTemplate redisTemplate;

	//从数据库中查询数据到页面中（广告的列表）
    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
    	//先取出数据
		List<TbContent> contents = null;
		try {
			contents = (List)redisTemplate.boundHashOps(PinyougouConstants.SPRING_DATA_REDIS_HASH_KEY_CONTENT).get(categoryId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(contents!=null) {
			System.out.println("有缓存。。。。。。。。。。。。");
			//判断缓存是否有数据  如果有   直接返回
			return contents;
		}

		//如果没有 从数据库查询
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);//
		criteria.andStatusEqualTo("1");//有效的内容
		//排序
		example.setOrderByClause("sort_order");//select *from tcontent order by sort_order desc
		List<TbContent> tbContents = contentMapper.selectByExample(example);

		//注意：加入缓存不能影响正常的业务逻辑
		//写入缓存
		try {
			System.out.println("没有缓存，需要从数据库中查询");
			redisTemplate.boundHashOps(PinyougouConstants.SPRING_DATA_REDIS_HASH_KEY_CONTENT).put(categoryId,tbContents);
		} catch (Exception e) {
			e.printStackTrace();
		}


		return tbContents;
    }

}
