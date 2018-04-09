

app.controller('itemController',function($scope){
	
	/**
	 * 
	 * @param {Object} x 要减 或者要加的值
	 */
	$scope.addNum=function(x){
		
		$scope.num=parseInt($scope.num);//转成数字
		x=parseInt(x);//转成数字
		$scope.num=$scope.num+x;
		if($scope.num<=1){
			$scope.num=1;
		}
		
	}
	
	$scope.specificationItems={};
	
	/**
	 * 选中规格
	 * @param {Object} name
	 * @param {Object} value
	 */
	$scope.selectSpecification=function(name,value){
		$scope.specificationItems[name]=value;
		//去搜索 当前被点击的对象是否在 SKU的列表中存在，如果存在 说明 展示数据
		searchSku();
	}
	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.sku=skuList[0];//展示第一个SKU的对象
	
	$scope.specificationItems=angular.fromJson(angular.toJson($scope.sku.spec)) ;
	
	//去搜索 当前被点击的对象是否在 SKU的列表中存在，如果存在 说明 展示数据
	searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			if(matchObject( $scope.specificationItems,skuList[i].spec)){
					$scope.sku=skuList[i];
			}
			
//			if(angular.toJson( $scope.specificationItems)==angular.toJson(skuList[i].spec)){
//				console.info("用户选择的规格绑定："+angular.toJson( $scope.specificationItems));
//				
//				console.info("循环遍历的SKU对象："+angular.toJson(skuList[i].spec));
//				$scope.sku=skuList[i];//sku绑定了要显示的标题  显示的价格
//			}
		}
	}
	
	
	
	matchObject=function(map1,map2){		
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}			
		}
		
		
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}			
		}
		return true;		
	}

	
})