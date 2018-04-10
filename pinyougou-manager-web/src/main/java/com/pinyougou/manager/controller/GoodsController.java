package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

//	@Reference
//	private ItemPageService itemPageService;

	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
//			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
//			发送消息 删除索引
			jmsTemplate.send(queue_solr_delete, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			//发送消息 删除静态页面

			jmsTemplate.send(topic_delete_html, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});


			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

//	@Reference
//	private ItemSearchService itemSearchService;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Resource(name="queue_solr_update")
	private Destination queue_solr_update;

	@Resource(name="topic_gener_html")
	private Destination topic_gener_html;
	@Resource(name="queue_solr_delete")
	private Destination queue_solr_delete;

	@Resource(name="topic_delete_html")
	private Destination topic_delete_html;


	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids ,String status){
		try {
			goodsService.updateStatus(ids,status);
			//这里添加导入数据库的数据到索引库中
			//1.查询被审核过的商品的数据   如果是 0 应该要将索引库的数据删除
			if("1".equals(status)) {//要审核 才需要更新
				List<TbItem> list = goodsService.findItemListByGoodsIdandStatus(ids, "1");
				//2.将查询出来的商品的数据 导入到索引库中
//				itemSearchService.importItemListData(list);

				jmsTemplate.send(queue_solr_update, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(JSON.toJSONString(list));
					}
				});

				//3.调用商品的静态化服务 生成静态页面
//				for (Long id : ids) {
//					itemPageService.genItemHtml(id);
//				}
				jmsTemplate.send(topic_gener_html, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});


			}
			return new Result(true, "审核成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "审核失败");
		}
	}

	//测试方法（生成静态页面）

//	@RequestMapping("/genHtml")
//	public void generator(Long goodsId){
//			//调用生成静态页面的方法
//		itemPageService.genItemHtml(goodsId);
//	}

}
