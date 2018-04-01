package com.pinyougou.sellergoods.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbBrandMapper brandMapper ;

	@Autowired
	private TbItemMapper itemMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//1.先要插入商品表（SPU表）返回主键的值
		TbGoods tbGoods = goods.getGoods();
		tbGoods.setAuditStatus("0");//表示刚添加的商品未审核
		goodsMapper.insert(tbGoods);

		//2.插入商品描述表 需要用到上次放回的主键
		goods.getGoodsDesc().setGoodsId(tbGoods.getId());
		goodsDescMapper.insert(goods.getGoodsDesc());

		//3.插入SKU列表（根据选中的规格生成的）
		List<TbItem> itemList = goods.getItemList();

		//判断 如果启用了规格 插入SKU列表

		//如果 没有 插入一条记录
		if("1".equals(tbGoods.getIsEnableSpec())) {
			for (TbItem item : itemList) {
				//设置标题 spuname+" "+规格
				String itemSpec = item.getSpec();
				Map<String, Object> specObject = JSON.parseObject(itemSpec, Map.class);
				String title = goods.getGoods().getGoodsName();
				for (String key : specObject.keySet()) {
					String specOptionName = (String) specObject.get(key);
					title += " " + specOptionName;
				}

				item.setTitle(title);

				//设置图片Wie一张

				if (goods.getGoodsDesc().getItemImages() != null && goods.getGoodsDesc().getItemImages().length() > 0) {
					String itemImages = goods.getGoodsDesc().getItemImages();
					List<Map> images = JSON.parseArray(itemImages, Map.class);
					item.setImage((String) images.get(0).get("url"));
				}

				//设置三级分类
				item.setCategoryid(tbGoods.getCategory3Id());
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
				item.setCategory(itemCat.getName());//三级分类的名称

				item.setCreateTime(new Date());
				item.setUpdateTime(item.getCreateTime());


				item.setGoodsId(tbGoods.getId());

				item.setSellerId(tbGoods.getSellerId());
				TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
				item.setSeller(seller.getNickName());//店铺名称
				TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
				item.setBrand(brand.getName());//品牌名称
				//插入
				itemMapper.insert(item);
			}
		}else{
			//插入一个单品
			TbItem item =new TbItem();

			item.setSellerId(tbGoods.getSellerId());
			TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
			item.setSeller(seller.getNickName());//店铺名称

			item.setTitle(tbGoods.getGoodsName());//SPU mingch

			item.setPrice(tbGoods.getPrice());
			item.setNum(9999);



			if (goods.getGoodsDesc().getItemImages() != null && goods.getGoodsDesc().getItemImages().length() > 0) {
				String itemImages = goods.getGoodsDesc().getItemImages();
				List<Map> images = JSON.parseArray(itemImages, Map.class);
				item.setImage((String) images.get(0).get("url"));
			}

			item.setCategoryid(tbGoods.getCategory3Id());
			TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
			item.setCategory(itemCat.getName());//三级分类的名称

			item.setCreateTime(new Date());
			item.setUpdateTime(item.getCreateTime());

			item.setStatus("1");//启用
			item.setIsDefault("1");
			item.setGoodsId(tbGoods.getId());

			TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
			item.setBrand(brand.getName());//品牌名称
			item.setSpec("{}");

			itemMapper.insert(item);

		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){

		//1.更新spu
		TbGoods tbGoods = goods.getGoods();
		goodsMapper.updateByPrimaryKey(tbGoods);
		//2.更新商品的描述
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		System.out.println("desc的商品的ID"+goodsDesc.getGoodsId());
		goodsDesc.setGoodsId(tbGoods.getId());
		goodsDescMapper.updateByPrimaryKey(goodsDesc);
		//3.更新SKU列表
		//根据spu商品的ID查询该SPU的SKU的列表  删除
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(tbGoods.getId());// from tbitem where goodsid=1

		itemMapper.deleteByExample(example);
		//插入最新的SKU的列表（页面传递过来的）
		List<TbItem> itemList = goods.getItemList();
		if("1".equals(tbGoods.getIsEnableSpec())) {
			for (TbItem item : itemList) {
				//设置标题 spuname+" "+规格
				String itemSpec = item.getSpec();
				Map<String, Object> specObject = JSON.parseObject(itemSpec, Map.class);
				String title = goods.getGoods().getGoodsName();
				for (String key : specObject.keySet()) {
					String specOptionName = (String) specObject.get(key);
					title += " " + specOptionName;
				}

				item.setTitle(title);

				//设置图片Wie一张

				if (goods.getGoodsDesc().getItemImages() != null && goods.getGoodsDesc().getItemImages().length() > 0) {
					String itemImages = goods.getGoodsDesc().getItemImages();
					List<Map> images = JSON.parseArray(itemImages, Map.class);
					item.setImage((String) images.get(0).get("url"));
				}

				//设置三级分类
				item.setCategoryid(tbGoods.getCategory3Id());
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
				item.setCategory(itemCat.getName());//三级分类的名称

				item.setCreateTime(new Date());
				item.setUpdateTime(item.getCreateTime());


				item.setGoodsId(tbGoods.getId());

				item.setSellerId(tbGoods.getSellerId());
				TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
				item.setSeller(seller.getNickName());//店铺名称
				TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
				item.setBrand(brand.getName());//品牌名称
				//插入
				itemMapper.insert(item);
			}
		}else{
			//插入一个单品
			TbItem item =new TbItem();

			item.setSellerId(tbGoods.getSellerId());
			TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
			item.setSeller(seller.getNickName());//店铺名称

			item.setTitle(tbGoods.getGoodsName());//SPU mingch

			item.setPrice(tbGoods.getPrice());
			item.setNum(9999);



			if (goods.getGoodsDesc().getItemImages() != null && goods.getGoodsDesc().getItemImages().length() > 0) {
				String itemImages = goods.getGoodsDesc().getItemImages();
				List<Map> images = JSON.parseArray(itemImages, Map.class);
				item.setImage((String) images.get(0).get("url"));
			}

			item.setCategoryid(tbGoods.getCategory3Id());
			TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
			item.setCategory(itemCat.getName());//三级分类的名称

			item.setCreateTime(new Date());
			item.setUpdateTime(item.getCreateTime());

			item.setStatus("1");//启用
			item.setIsDefault("1");
			item.setGoodsId(tbGoods.getId());

			TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
			item.setBrand(brand.getName());//品牌名称
			item.setSpec("{}");

			itemMapper.insert(item);

		}


	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//1.查询商品的信息(spu)
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//2.查询商品的描述信息（goodsdesc）
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		//3.查询商品的SKU列表
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(id);
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		goods.setItemList(tbItems);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//逻辑删除
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);
//			goodsMapper.deleteByPrimaryKey(id);
		}		
	}
	

//	@Transactional(rollbackFor = Exception.class,)//表示如果是exception异常就开启事务管理
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		//查询的时候需要查询没有被删除的
		criteria.andIsDeleteIsNull();// isdele is null
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    @Override
    public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			//update tbgoods set stauts=1 where id in (ids)
			//查询商品的数据 获取到了状态
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			//更新状态
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

}
