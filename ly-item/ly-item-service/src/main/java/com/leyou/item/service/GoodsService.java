package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface GoodsService {

    PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key);

    void saveGoods(Spu spu);

    SpuDetail queryDetailBySpuId(Long spuId);

    List<Sku> querySkuListBySpuId(Long spuId);

    void updateGoods(Spu spu);
}
