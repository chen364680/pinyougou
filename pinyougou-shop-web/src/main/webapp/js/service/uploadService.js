app.service('uploadService',function ($http) {

    /**
     * 上传图片
     * @returns {*}
     */
    this.uploadFile=function () {
        var formData=new FormData();//h5的表单对象
        //var fileobject= document.getElementById("file");
        // formData.append("file",fileobject.files[0]);--->File对象

        formData.append("file",file.files[0]);
        //key=value&ky2=value   file:是要和controoller.java中的参数的名字要一致
        //第二个file 要和页面中的Input type="file" 中的id一致
        return $http({
            method:'POST',
            url:"../upload.do",
            data: formData,
            headers: {'Content-Type':undefined},
            //默认的时候，angularj 发送的contety-type:application/json; 如果使用了undefined 浏览器自懂变成 媒体类型 自动添加一个分隔符。
            transformRequest: angular.identity
            //以anglarjs 流的序列化方式序列化请求
        });

    }
})