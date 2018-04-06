app.service('contentService',function ($http) {
    this.findByCategoryId=function (id) {
        return $http.get('/content/findByCategoryId.do?categoryId='+id);
    }
});