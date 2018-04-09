package com.pinyougou.test;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.test
 * @company www.itheima.com
 */
public class FreemarkerGenerator {

    public static void main(String[] args) throws Exception {
        //输出模板的html的页面  需要 模板 + 数据集 =html

        //1.创建一个配置的对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //2.设置模板的所在的目录
        configuration.setDirectoryForTemplateLoading(new File("C:\\Users\\ThinkPad\\pinyougou\\pinyougouparent\\pinyougou-shop-web\\src\\main\\webapp\\WEB-INF\\ftl"));
        //3.设置模板文件编码格式 UTF-8
        configuration.setDefaultEncoding("utf-8");

        //4.创建一个模板   官方推荐使用的后缀是：.ftl 实际上可以是任意的  获取模板对象
        //参数：一定是相对路径
        Template template = configuration.getTemplate("template.ftl");
        //5.创建数据集
        Map model = new HashMap();
        model.put("name","特朗普不靠谱");
        model.put("date",new Date());
        //6.输出html页面
        FileWriter out = new FileWriter(new File("G:\\item\\hello.html"));

        //7.调用freemarker生成文本文件的方法
        template.process(model,out);



        //关闭流 ：应该在finally中关闭
        out.close();

    }
}
