package com.leyou.search.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.item.client.BrandClient;
import com.leyou.item.client.CategoryClient;
import com.leyou.item.client.GoodsClient;
import com.leyou.item.client.SpecClient;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecClient specClient;

    @Autowired
    private GoodsClient goodsClient;
    /**
     * 接收一个spu,返回一个Goods
     * @param spu
     * @return
     */
    @Override
    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();
//        查询分类
        String categoryNames = categoryClient.queryByIdList(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                .stream().map(Category::getName)
                .collect(Collectors.joining(" "));
//        查询品牌
        String brandName = brandClient.queryById(spu.getBrandId()).getName();
//        所有搜索字段拼接，标题，分类，品牌，规格参数
        String all = spu.getSubTitle() + categoryNames + brandName;//TODO 规格参数

//        查询sku
        List<Sku> skuList = goodsClient.querySkuListBySpuId(spuId);

//        准备一个list，里面是map，用map代替sku对象
        List<Map<String,Object>> skuMap  = new ArrayList<>();
        skuList.forEach(sku -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("images",sku.getImages());
            map.put("price",sku.getPrice());
            map.put("title",sku.getTitle());
            skuMap.add(map);
        });
//        获取价格
        Set<Long> priceSet = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());

//        准备规格参数，规格参数的key在SpecParam中，规格参数的值在SpuDetail
        Map<Object, Object> specs = new HashMap<>();
//        规格参数的key,可以从SpecParam中获取
        List<SpecParam> specParams = specClient.queryParam(null, spu.getCid3(), true);
//        规格参数的值，从SpuDetail中获取
        SpuDetail spuDetail = goodsClient.queryDetailBySpuId(spuId);
//        获取通用规格参数的值
        Map<Long, Object> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, Object.class);
//         获取特殊规格参数值
        Map<Long, List<String>> specialSpec =
                JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        for (SpecParam param : specParams) {
//            获取规格参数名称，作为key
            String key = param.getName();
//            规格参数值
            Object value = null;
//            判断是否是通用
            if(param.getGeneric()){
//               通用规格参数
                value = genericSpec.get(param.getId());
//               判断是否是数值类型,数值类型的在索引库中要存所属的规格参数的区间值（供用户进行选项点击搜索）
                if(param.getNumeric()){
//                    判断分段是否分割
                    value = chooseSegment(value.toString(), param);
                }
            }else{
//                特有规格参数（用户在搜索框总输入的，例如颜色之类的属于sku特有规格参数，进行搜索）
                value = specialSpec.get(param.getId());
            }
            value = value == null ? "其他" : value ;
//            存入规格参数map
            specs.put(key,value);
        }


//        创建一个Goods
        Goods goods = new Goods();
        goods.setAll(all);// 所有搜索字段拼接
        goods.setBrandId(spu.getBrandId());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);
        goods.setPrice(priceSet);// 当前spu下的所有sku的价格集合
        goods.setSkus(JsonUtils.toString(skuMap));// 当前spu下所有sku的json数组
        goods.setSpecs(null);//TODO 可以用来搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    /**
     *将数值类型的规格参数转换为区间
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}
