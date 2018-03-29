//控制层
app.controller('goodsController', function ($scope, $controller, goodsService,uploadService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //商品的添加
    $scope.add = function () {

        $scope.entity.goodsDesc.introduction=editor.html();

        var serviceObject = goodsService.add($scope.entity);//增加

        serviceObject.success(
            function (response) {
                if (response.success) {
                    //清空
                  $scope.entity={};
                    editor.html('');
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }


    $scope.uploadFile=function(){
        uploadService.uploadFile().success(function(response) {
            if(response.success){//如果上传成功，取出url
                $scope.image_entity.url=response.message;//设置文件地址
            }else{
                alert(response.message);
            }
        }).error(function() {
            alert("上传发生错误");
        });
    };
    $scope.entity={goods:{},goodsDesc:{itemImages:[]}};
    //保存图片的路径和颜色---》封装到对象中，将对象push到数组中
    $scope.add_image_entity=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }


});	
