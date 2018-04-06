app.service('searchService',function ($http) {

    /**
     * 根据查询条件（封装在map）查询
     * @param searchMap
     */
    this.search=function (searchMap) {
       return $http.post('/itemsearch/search.do',searchMap);
    }

})