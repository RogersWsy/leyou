package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;

public interface BrandService {
    public PageResult<Brand> queryByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key);

    void saveBrand(Brand brand, List<Long> cids);

    Brand queryByid(Long id);

    List<Brand> queryByCid(Long cid);
}
