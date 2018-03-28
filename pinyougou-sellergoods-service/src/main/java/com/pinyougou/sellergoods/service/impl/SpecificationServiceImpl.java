package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
			return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private TbSpecificationOptionMapper optionMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {

		//1.插入规格表  生成一个主键返回到POJO中
		TbSpecification specification1 = specification.getSpecification();
		specificationMapper.insert(specification1);

		System.out.println(specification1.getId());
		//2.插入规格选项表 指定规格的ID

		List<TbSpecificationOption> optionList = specification.getSpecificationOptionList();

		for (TbSpecificationOption option : optionList) {
			//设置规格的ID
			option.setSpecId(specification1.getId());
			optionMapper.insert(option);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){

		//更新规格对象
		TbSpecification specification1 = specification.getSpecification();
		specificationMapper.updateByPrimaryKey(specification1);

		//更新规格选项   [{},{}]   先删除原来的规格选项列表  再添加新的规格选项列表

		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		example.createCriteria().andSpecIdEqualTo(specification1.getId());//条件

		optionMapper.deleteByExample(example);//删除原来的

		//先获取到从页面传递过来的规格选项列表
		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();

		for (TbSpecificationOption option : specificationOptionList) {
			option.setSpecId(specification1.getId());
			optionMapper.insert(option);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//1.查询规格的数据
		TbSpecification tbspecification = specificationMapper.selectByPrimaryKey(id);
		//2.根据规格的ID 查询规格选项的列表
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		example.createCriteria().andSpecIdEqualTo(tbspecification.getId());
		List<TbSpecificationOption> optionsList = optionMapper.selectByExample(example);
		//3.设置到定义的包装对象中
		Specification specification = new Specification();
		specification.setSpecification(tbspecification);
		specification.setSpecificationOptionList(optionsList);
		
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);
			//删除规格的选项  delete from option where specId=1
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			example.createCriteria().andSpecIdEqualTo(id);
			optionMapper.deleteByExample(example);

		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    @Override
    public List<Map> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
