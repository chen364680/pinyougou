app.controller('seckillGoodsController' ,function($scope,$location,$interval,seckillGoodsService){
    //读取列表数据绑定到表单中
    $scope.findList=function(){
        seckillGoodsService.findList().success(
            function(response){
                $scope.list=response;//绑定变量
            }
        );
    }

    //根据商品的ID 查询商品的信息
    $scope.findOne=function () {
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {//获取到的秒杀的商品的对象
                $scope.entity = response;
                //再进行倒计时的数值展示
                //当前距离结束时间的剩余数据
                allsecond = new Date($scope.entity.endTime).getTime()/1000-new Date().getTime()/1000;//存储是毫秒 是从1970到结束时间的毫秒数
                allsecond=Math.floor(allsecond);
                time = $interval(function(){

                    $scope.timeString = convertTimeString(allsecond);
                    if(allsecond>0){
                        allsecond =allsecond-1;
                    }else{
                        $interval.cancel(time);
                        alert("秒杀服务已结束");
                    }
                },1000);
            }
        )
    }

    convertTimeString=function(allsecond){
        var days= Math.floor( allsecond/(60*60*24));//天数
        var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
        var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
        var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        if(days>0){
            days=days+"天 ";
        }else{
            days="";
        }
        if(hours<10){
            hours="0"+hours;
        }
        if(minutes<10){
            minutes="0"+minutes;
        }
        if(seconds<10){
            seconds="0"+seconds;
        }
        return days+hours+":"+minutes+":"+seconds;
    }
    
    $scope.submitOrder=function (seckillId) {
        seckillGoodsService.submitOrder(seckillId).success(
            function (response) {//result
                if(response.success){
                    window.location.href="pay.html";//支付页面
                }else{
                    alert(response.message);
                }

            }
        )
    }



});
