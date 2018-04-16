app.service('itemService',function ($http) {
    this.addGoodsToCartList=function (itemId,num) {
       return $http.get('/cart/addGoodsToCartList.do?itemId='+itemId+"&num="+num);
    }
})