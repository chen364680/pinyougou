app.controller('cartController',function($scope,cartService){
    //查询购物车列表
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){//List<cart>
                $scope.cartList=response;//购物车的列表
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

    $scope.findAdressList=function () {
        cartService.findAdressList().success(
            function (response) {//地址列表
                $scope.addressList = response;

                for(var i=0;i< $scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address= $scope.addressList[i];
                        break;
                    }
                }

            }
        )
    }
    //选中地址
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    
    $scope.isSelectedAddress=function (address) {
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }

    $scope.order={paymentType:'1',sourceType:'2'};//表示微信支付 还有设置订单的来源 就是PC端
    
    $scope.selectPayType=function (paymentType) {
        $scope.order.paymentType=paymentType;
    }

    $scope.submitOrder=function () {
        //将地址存入到order对象中
        $scope.order.receiverAreaName=$scope.address.address;
        $scope.order.receiverMobile=$scope.address.mobile;
        $scope.order.receiver=$scope.address.contact;
        cartService.submitOrder($scope.order).success(
            function (response) {
                if(response.success){
                    alert("成功");
                    window.location.href="pay.html";
                }else{
                    alert("失败");
                }
            }
        )
    }




});
