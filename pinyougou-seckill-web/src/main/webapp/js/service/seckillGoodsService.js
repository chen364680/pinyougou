app.service("seckillGoodsService",function ($http) {
    //查询秒杀商品的列表（符合条件的列表）
    this.findList=function(){
        return $http.get('/seckillGoods/findList.do');
    }

    this.findOne=function (id) {
        return $http.get('/seckillGoods/findOne.do?id='+id);
    }
    
    this.submitOrder=function (seckillId) {
        return $http.get('/seckillOrder/submitOrder.do?seckillId='+seckillId);
    }


})