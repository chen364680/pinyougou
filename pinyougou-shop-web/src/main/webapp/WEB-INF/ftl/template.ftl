<#--注释-->

你好啊，${name}，欢迎来到黑马


<#--assign 定义变量 以及赋予值-->

<#assign personname="本拉登"  >
<br/>
<h1>
    他的名字是：${personname}
</h1>

<#assign info={"id":1,"text":"彩色"}  >
<br/>
${info.id},${info.text}

<#include "head.ftl">


<#assign flag=true>




<#if !flag>
    <h2>标题2</h2>

<#else >
    <h3>标题3</h3>
</#if>

<#--遍历-->

<#assign mylist=[{"id":1},{"id":2}]>

<#list mylist as item>
    <h1>${item.id}</h1>
    <h2>下标：${item_index+1}</h2>
    <h3>下标：${item?index}</h3>
</#list>
<br>
获取集合的条数：${mylist?size}

<br>
<#assign shuzi="{\"id\":1,\"name\":\"奥巴马\"}">
${shuzi}
${shuzi?eval.name}
<br>
显示日期：
仅仅是日期：${date?date}<br>
仅仅是时间：${date?time}<br>
日期和时间：${date?datetime}<br>
自定义日期格式：${date?string("yyyy/MM/dd HH:mm:ss")}


<br>
<#assign number=123917329173921739>
${number?c}

${nullkey!'默认值'}
${nullkey!}

<#if nullkey??>
    如果不是空我就应该出现在这里
    <#else >
    如果是空给我就应该出现在这里
</#if>

<#if (number > 5)>
number大于了5啦
<#else >
number小于了5啦
</#if>











