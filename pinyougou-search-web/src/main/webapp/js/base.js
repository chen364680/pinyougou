var app = angular.module('pinyougou', []);//定义模块


// app.filter('trustHtml',['$sce',function () {
//
// }])

app.filter('trustHtml',function ($sce) {
    return function (data) {
           return $sce.trustAsHtml(data);//data是原数据带有Html标签的
    }
})