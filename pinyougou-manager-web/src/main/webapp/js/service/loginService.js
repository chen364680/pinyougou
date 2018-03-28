app.service('loginService',function ($http) {
    this.getLoginInfo=function () {
        return $http.get('../login/getLoginName.do');
    }
});