//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

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
    $scope.findOne = function () {
        //通过$location来接收参数ID值
        var id = $location.search()['id'];
        console.info(">>>>>>" + id);
        if (id == null || id == undefined) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;//goods={tbgoods,tggoodsdesc,itemList}
                $scope.entity.goodsDesc.itemImages = angular.fromJson($scope.entity.goodsDesc.itemImages);
                alert(">>>>>>>>>" + $scope.entity.goodsDesc.customAttributeItems);
                $scope.entity.goodsDesc.customAttributeItems = angular.fromJson($scope.entity.goodsDesc.customAttributeItems);
                alert(">>>>>" + $scope.entity.goodsDesc.specificationItems);
                $scope.entity.goodsDesc.specificationItems = angular.fromJson($scope.entity.goodsDesc.specificationItems);

               // var itemList = $scope.entity.itemList;//其中的属性spec 是一个字符串 需要转成json对象
                for (var i=0;i<$scope.entity.itemList.length;i++){
                    // $scope.entity.itemList[i].spec;//{"机身内存":"16G","网络":"联通3G"}
                    $scope.entity.itemList[i].spec= angular.fromJson($scope.entity.itemList[i].spec);
                }
                //把商品的介绍信息存储到kindeditor
               editor.html($scope.entity.goodsDesc.introduction);

            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        $scope.entity.goodsDesc.introduction=editor.html();

        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    // $scope.reloadList();//重新加载
                    alert("修改成功");
                    //清空kindeditor
                    editor.html('');
                    $scope.entity={};
                    //重新调转到列表
                    window.location.href="goods.html";

                } else {
                    alert(response.message);
                }
            }
        );
    }

    //商品的添加
    $scope.add = function () {

        $scope.entity.goodsDesc.introduction = editor.html();

        var serviceObject = goodsService.add($scope.entity);//增加

        serviceObject.success(
            function (response) {
                if (response.success) {
                    //清空
                    $scope.entity = {};
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


    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {//如果上传成功，取出url
                $scope.image_entity.url = response.message;//设置文件地址
            } else {
                alert(response.message);
            }
        }).error(function () {
            alert("上传发生错误");
        });
    };
    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], customAttributeItems: [], specificationItems: []}};

    //保存图片的路径和颜色---》封装到对象中，将对象push到数组中
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //方法  根据父节点的id查询该父节点的子节点列表
    $scope.selectItemCat1List = function (id) {
        itemCatService.findByParentId(id).success(
            function (response) {//List<tbitemcat>
                $scope.itemCat1List = response;
            }
        )
    }

    //可以监控某一个变量的变化 做相应的处理  监控一级分类变化  从数据库查询二级分类
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        if (newValue != null || newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {//List<tbitemcat>
                    $scope.itemCat2List = response;
                }
            )
        }
    })

    //监控二级分类 查询三级分类
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        if (newValue != null || newValue != undefined) {
            itemCatService.findByParentId(newValue).success(
                function (response) {//List<tbitemcat>
                    $scope.itemCat3List = response;
                }
            )
        }
    })
    //监控三级分类  查询分类关联的模板
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        //newVALUE就是分类的ID
        if (newValue != null || newValue != undefined) {
            itemCatService.findOne(newValue).success(
                function (response) {//resposne是tbitemcat 对象----》对象中的typeID
                    $scope.entity.goods.typeTemplateId = response.typeId;
                }
            )
        }
    })


    //监控模板的ID的值的变化 查询模板对象
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        //newvalue就是模板的ID
        //品牌列表展示
        if (newValue != null || newValue != undefined) {

            typeTemplateService.findOne(newValue).success(
                function (response) {//模板对象
                    //获取品牌数据
                    $scope.typeTemplate = response;
                    $scope.typeTemplate.brandIds = angular.fromJson($scope.typeTemplate.brandIds);

                    //获取扩展属性
                    //[{"text":"内存大小"},{"text":"颜色"}]
                    var id = $location.search()['id'];
                    if (id == null || id == undefined) {//要新增
                        $scope.entity.goodsDesc.customAttributeItems = angular.fromJson($scope.typeTemplate.customAttributeItems);
                    }
                }
            );

            //根据模板id获取模板的对象中的规格属性列表（包括规格的选项）
            typeTemplateService.findSpecList(newValue).success(
                function (response) {//List<Map>
                    $scope.specList = response;
                }
            )

        }
    })

    /**
     * 当点击复选框的时候调用  去影响 变量 entity.goodsDesc.specificationItems=[{"attributeName":"机身内存","attributeValue":["16G","32G"]}]
     */
    $scope.updateSpecAttribute = function ($event, specName, specValue) {
        //1.从已有的数组中获取里面的对象
        var objectByKey = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', specName);//从数组中查询是否存在对象
        if (objectByKey != null) {
            //{"attributeName":"机身内存","attributeValue":["16G","32G"]}
            //有 在已有的对象中添加规格选项
            if ($event.target.checked) {//选中啦
                objectByKey.attributeValue.push(specValue);
            } else {
                objectByKey.attributeValue.splice(objectByKey.attributeValue.indexOf(specValue), 1);
                if (objectByKey.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(objectByKey), 1);
                }
            }
        } else {
            //重新构建
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": specName, "attributeValue": [specValue]});
        }


        $scope.createItemList = function () {
            //1.构建一个初始化的对象
            $scope.entity.itemList = [{spec: {}, price: 0, num: 9999, status: '0', isDefault: '0'}];

            //2.循环遍历
            var items = $scope.entity.goodsDesc.specificationItems;

            for (var i = 0; i < items.length; i++) {
                //{"attributeName":"机身内存","attributeValue":["16G","32G"]}
                var object = items[i];
                $scope.entity.itemList = addColumn($scope.entity.itemList, object.attributeName, object.attributeValue);//返回的是新的对象
            }
        }


        //重新构建一个新的对象itemList
        //entity.itemList=[
        //     {spec:{},price:0,num:9999,status:'0',isDefault:'0'}
        //    ]
        addColumn = function (itemList, columnName, columnValues) {//columnName-->机身内存
            //重新构建
            var newList = [];

            for (var i = 0; i < itemList.length; i++) {
                var oldRow = itemList[i];
                for (var j = 0; j < columnValues.length; j++) {//["16G","32G"]
                    var newRow = angular.fromJson(angular.toJson(oldRow));//深克隆
                    //添加规格和规格选项
                    newRow.spec[columnName] = columnValues[j];
                    newList.push(newRow);
                }
            }
            return newList;
        }
    }


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
     * 查询判断 在已有的列表中查询 从数据库中找到的数据 如果匹配 就返回true ，
     */
    $scope.checkAttributeValue = function (specName, specOptionName) {
        //获取从数据库中查询到的数组列表
        var items = $scope.entity.goodsDesc.specificationItems;
        //var object = items[i];//{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"}
        var object = $scope.searchObjectByKey(items, "attributeName", specName);
        if (object == null) {
            return false;
        } else {
            var attributeValue = object.attributeValue;
            for (var i = 0; i < attributeValue.length; i++) {
                if (specOptionName == attributeValue[i]) {
                    return true;
                }
            }
        }
        return false;
    }


});
