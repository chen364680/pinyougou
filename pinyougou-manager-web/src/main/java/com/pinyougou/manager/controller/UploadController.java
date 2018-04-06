package com.pinyougou.manager.controller;

import com.pinyougou.common.util.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.shop.controller
 * @company www.itheima.com
 */
@RestController
public class UploadController {

    @Value("${IMAGE_SERVER_URL}")
    private String IMAGE_SERVER_URL;

    @RequestMapping("/upload")
    public Result uploadFile(MultipartFile file){

        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //第一个参数是字节数组
            byte[] bytes = file.getBytes();//文件流对象
            String orginname= file.getOriginalFilename();//原来的文件的名：xxxx.jgp
            String extName = orginname.substring(orginname.indexOf(".") + 1);//获取扩展名
            //第二个参数是文件的扩展名
            String uploadFile = fastDFSClient.uploadFile(bytes, extName);//     group1/M00/00/04/wKgZhVq8Z5SAGUd_AAIbczWvGxA018.jpg
            String url =  IMAGE_SERVER_URL+uploadFile;//URL
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"服务器上传失败");
        }
    }
}
