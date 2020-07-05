package com.leyou.item.client;

import com.leyou.item.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")
public interface CategoryClient {
//    feign 利用动态代理对这个接口进行实现，利用反射获取注解上的信息，
//    请求路径也就是http://item-service/category/list/ids?ids=  最终利用eureka拉取服务item-service对应的列表，利用负载均衡取一个IP地址
    @GetMapping("category/list/ids")
    List<Category> queryByIdList(@RequestParam("ids") List<Long> idList);
}
