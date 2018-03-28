package com.pinyougou.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.shop.service
 * @company www.itheima.com
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    //从数据库中获取用户的信息
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //业务逻辑就是：从该数据库获取用户的信息  判断 用户名和密码是否正确  判断用户是否有权限 判断用户是否已经被冻结 ..........
        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        System.out.println("进过了自定义的认证类");
        //判断用户在数据库中是否存存在
        TbSeller tbSeller = sellerService.findOne(username);
        if(tbSeller==null){
            return null;
        }

        if(!"1".equals(tbSeller.getStatus())){
            return null;
        }
        //判断如果存在 需要判断状态是否是已经审核
        System.out.println("至少用户有和状态值是1");
        return new User(username,tbSeller.getPassword(),grantedAuths);
    }
}
