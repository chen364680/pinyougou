package com.pinyougou.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sound.midi.Soundbank;
import java.util.List;
import java.util.Set;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.test
 * @company www.itheima.com
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-redis.xml")
public class DataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void testValue(){//String
        //redis = new reids  redis.set
        redisTemplate.boundValueOps("key1").set("hello wolrd");
        System.out.println(redisTemplate.boundValueOps("key1").get());
        redisTemplate.delete("key1");
        System.out.println(">>>>"+redisTemplate.boundValueOps("key1").get());
    }

    //set集合
    @Test
    public void testSetValue(){
        redisTemplate.boundSetOps("setkey1").add("张三");
        redisTemplate.boundSetOps("setkey1").add("李四");
        redisTemplate.boundSetOps("setkey1").add("王五");
    }

    @Test
    public void testSetMembers(){
        Set members = redisTemplate.boundSetOps("setkey1").members();
        for (Object member : members) {
            System.out.println(member);
        }
    }


    //删除集合中的元素
    @Test
    public void testSetDelete(){
        Long remove = redisTemplate.boundSetOps("setkey1").remove("张三");
        System.out.println(remove);
    }


    @Test
    public void testSetValue1(){
        redisTemplate.boundListOps("namelist1").rightPush("刘备");
        redisTemplate.boundListOps("namelist1").rightPush("关羽");
        redisTemplate.boundListOps("namelist1").rightPush("张飞");
    }

    @Test
    public void testlistValue1(){
        redisTemplate.boundListOps("namelist1").leftPush("曹操");

    }

    @Test
    public void testRange(){
        List namelist1 = redisTemplate.boundListOps("namelist1").range(0, -1);//表示查所有的元素

        for (Object o : namelist1) {
            System.out.println(o);
        }
    }

    @Test
    public void testRemoveByIndex(){
        //参数1：删除的个数
        //参数2：要删除的元素
        redisTemplate.boundListOps("namelist1").remove(1, "曹操");
    }

//key1---field1--value1
//key1---field2--value2

    @Test
    public void testhashValue(){
        redisTemplate.boundHashOps("namehash").put("a", "唐僧");//map
        redisTemplate.boundHashOps("namehash").put("b", "悟空");
        redisTemplate.boundHashOps("namehash").put("c", "八戒");
        redisTemplate.boundHashOps("namehash").put("d", "沙僧");
    }

    @Test
    public void testhashValueGet(){
        System.out.println(redisTemplate.boundHashOps("namehash").get("a"));

    }





}
