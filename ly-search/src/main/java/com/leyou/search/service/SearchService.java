package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;

public interface SearchService {
    public Goods buildGoods(Spu spu);

    PageResult<Goods> search(SearchRequest request);

}
