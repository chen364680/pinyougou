app.service('payService',function($http){
    //本地支付
    this.createNative=function(){
        return $http.get('/pay/createNative.do');
    }
    this.queryStatus=function (out_trade_no) {
        return $http.get('/pay/queryStatus.do?out_trade_no='+out_trade_no);
    }
});
