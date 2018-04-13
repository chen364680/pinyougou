app.controller('userController',function ($scope,userService) {

    //注册
    $scope.reg=function () {
        //校验密码和确认密码是否正确

        if($scope.confirmpassword==null || $scope.confirmpassword==undefined){
            alert("密码不能空");
            return ;
        }
        if($scope.entity.password==null || $scope.entity.password==undefined){
            alert("密码不能空");
            return ;
        }
        if($scope.confirmpassword!=$scope.entity.password){
            alert("密码不一致");
            return ;
        }


        userService.add($scope.entity,$scope.code).success(
            function (response) {
                //result
                if(response.success){
                    alert("注册成功");
                }else{
                    alert(response.message);
                }
            }
        )
    }
    //发送验证码
    $scope.sendSMS=function () {
        userService.sendSMS($scope.entity.phone).success(
            function (response) {
                if(response.success){
                    alert(response.message);
                }else{
                    alert(response.message);
                }
            }
        )
    }


})