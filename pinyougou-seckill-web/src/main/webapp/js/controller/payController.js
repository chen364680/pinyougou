app.controller('payController',function ($scope,$location,payService) {
    
    //方法 
    $scope.createNative=function () {
        payService.createNative().success(
            function (response) {//Map

                // resultMap.put("code_url",xmlMap.get("code_url"));
                // resultMap.put("out_trade_no",out_trade_no);
                // resultMap.put("total_fee",total_fee);
                $scope.out_trade_no=response.out_trade_no;
                $scope.total_fee=(response.total_fee/100).toFixed(2);
                //获取值 生成二维码
                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.code_url
                });
                //当生成了二维码就不停的发送请求 查询该订单是否已经支付完成
                queryStatus($scope.out_trade_no);
            }
        )
    }

    queryStatus=function (out_trade_no) {
        payService.queryStatus(out_trade_no).success(
            function (response) {//result
                if(response.success){//表示支付成功
                    window.location.href="paysuccess.html#?money="+$scope.total_fee;
                }else{

                    if(response.message=='支付超时'){
                        // 重新生成二维码
                        alert("超时");
                       // $scope.createNative();
                    }else{
                        window.location.href="payfail.html";
                    }
                }
            }
        )
    }

    $scope.getMoney=function(){
        if($location.search()['money']==undefined){
            return;
        }
        return $location.search()['money'];
    }



})