//控制层
app.controller('goodsController', function ($scope, $controller, goodsService, itemCatService) {

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


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.selectIds=[];
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


    $scope.searchEntity = {'auditStatus': '0'};
    //商品的状态数组
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];


    $scope.itemCatList = [];
    /**
     * 查询所有的商品的分类列表
     */
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {//response:就是List<tbitemcat>

                var itemCatList = response;//数组

                for (var i = 0; i < itemCatList.length; i++) {
                    var itemcat = itemCatList[i];
                    $scope.itemCatList[itemcat.id] = itemcat.name;
                }


            }
        )
    }

    /**
     * 审核状态
     * @param status
     */
    $scope.updateStatus = function (status) {
        goodsService.updateStatus($scope.selectIds, status).success(
            function (response) {
                if (response.success) {
                    $scope.selectIds=[];
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            }
        )
    }


});
