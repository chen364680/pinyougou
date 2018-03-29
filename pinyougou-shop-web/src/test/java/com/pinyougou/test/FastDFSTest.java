package com.pinyougou.test;

import com.pinyougou.common.util.FastDFSClient;
import org.csource.fastdfs.*;
import org.junit.Test;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.test
 * @company www.itheima.com
 */
public class FastDFSTest {

    //上传图片
    @Test
    public void testUpload() throws Exception{
        //1.创建配置文件 配置tracker_server的地址
        //2.加载配置文件
        ClientGlobal.init("C:\\Users\\ThinkPad\\pinyougou\\pinyougouparent\\pinyougou-shop-web\\src\\main\\resources\\config\\fdfs_client.conf");
        //3.创建trackerServer对象用trackerClient 对象来获取的
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        //4.构建一个storageServer对象 赋值为空
        StorageServer storageServer = null;

        //5.创建一个storageClient
        StorageClient storageClient = new StorageClient(trackerServer,storageServer);

        /**
         * 第一个参数：要上传的文件的路径
         * 第二个参数：文件的扩展名 不带点
         * 第三个参数：文件的元数据
         */
        String[] strings = storageClient.upload_file("C:\\Users\\Public\\Pictures\\Sample Pictures\\Koala.jpg", "jpg", null);

        for (String string : strings) {
            System.out.println(string);
        }
    }


    @Test
    public void testFastClient() throws Exception{
        FastDFSClient client = new FastDFSClient("C:\\Users\\ThinkPad\\pinyougou\\pinyougouparent\\pinyougou-shop-web\\src\\main\\resources\\config\\fdfs_client.conf");
        String uploadFile = client.uploadFile("C:\\Users\\ThinkPad\\Pictures\\1e17d3ca7dffd144d3b41a991d81cfbe_b.jpg");
        System.out.println(uploadFile);
    }
}
