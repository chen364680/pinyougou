app.controller('indexController',function ($scope,loginService) {
    $scope.getLoginInfo=function () {
        loginService.getLoginInfo().success(
            function (response) {
                //response=map
                $scope.loginName = response.loginName;
    }
        )
    }
})