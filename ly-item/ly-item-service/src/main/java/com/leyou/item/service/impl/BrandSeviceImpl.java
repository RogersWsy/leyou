package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandSeviceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public PageResult<Brand> queryByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
//        1 分页
        PageHelper.startPage(page,rows);
//        2 条件过滤
        Example example = new Example(Brand.class);
        if(StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("name","%"+key+"%")
                    .orEqualTo("letter",key);
        }

//        3 排序
        if(StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
//        4 查询
        List<Brand> brandList = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
//        5 封装
        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);
        return new PageResult<>(pageInfo.getTotal(),brandList);
    }

    @Transactional
    @Override
    public void saveBrand(Brand brand, List<Long> cids) {
        int count = brandMapper.insert(brand);
        if(count != 1){
            throw new LyException(ExceptionEnum.INSERT_BRAND_ERROR);
        }
        for (Long cid : cids) {
            brandMapper.insertCategoryBrand(cid,brand.getId());
            if(count != 1){
                throw new LyException(ExceptionEnum.INSERT_BRAND_ERROR);
            }
        }
    }

    @Override
    public Brand queryByid(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    @Override
    public List<Brand> queryByCid(Long cid) {
        List<Brand> brandList = brandMapper.queryByCategoryId(cid);
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brandList;
    }


}
