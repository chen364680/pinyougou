app.controller('cartController',function($scope,cartService){
    //查询购物车列表
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){//List<cart>
                $scope.cartList=response;
                $scope.sum();
            }
        );
    }

    $scope.addGoodsToCartList=function (num,itemId) {
        cartService.addGoodsToCartList(num,itemId).success(
            function (response) {//result
                if(response.success){
                    $scope.findCartList();
                }else{
                    alert("添加失败");
                }
            }
        )
    }

    $scope.sum=function () {
        $scope.num=0
        $scope.totalMoney=0
        var cartlist = $scope.cartList;
        for(var i =0;i<cartlist.length;i++){
            var cart = cartlist[i];
            var orderItemList = cart.orderItemList;
            for(var j =0;j<orderItemList.length;j++){
                var item = orderItemList[j];
                $scope.num+=item.num;
                $scope.totalMoney+=item.totalFee;
            }
        }
    }
});
