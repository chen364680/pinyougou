app.controller('homeIndexController',function ($scope,loginService) {
    $scope.getLoginName=function () {
        loginService.getLoginName().success(
            function (response) {
                //map
                $scope.loginMap = response;
            }
        )
    }
})