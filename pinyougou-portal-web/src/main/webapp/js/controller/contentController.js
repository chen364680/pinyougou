app.controller('contentController',function ($scope,contentService) {
        //查询根据内容分类的ID查询内容列表
    $scope.contentList=[];
    $scope.findByCategoryId=function (id) {
        contentService.findByCategoryId(id).success(
            function (response) {//list<tbcontent>
                $scope.contentList[id] = response;
            }
        )
    }
    //右边小广告
    // $scope.findByCategoryId=function (id) {
    //     contentService.findByCategoryId(id).success(
    //         function (response) {//list<tbcontent>
    //             $scope.contentList[id] = response;
    //         }
    //     )
    // }
    // //中广告
    // $scope.findByCategoryId=function (id) {
    //     contentService.findByCategoryId(id).success(
    //         function (response) {//list<tbcontent>
    //             $scope.contentList[id] = response;
    //         }
    //     )
    // }
    
    $scope.search=function () {
        //发送请求 搜索数据
        window.location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;//文档一加载 立马发送请求 搜索数据展示。
    }

});