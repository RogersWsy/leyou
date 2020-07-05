package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Override
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
//      1   分页
        PageHelper.startPage(page,rows);
//      2   过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

//      2.1 关键字过滤
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%" + key + "%");
        }
//      2.2 上下架过滤
        if(saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }
//      2.3 过滤被逻辑删除的数据
        criteria.andEqualTo("valid",true);

//      3   排序（按更新时间）
        example.setOrderByClause("last_update_time DESC");
//      4   查询结果
        List<Spu> spuList = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
//      5   处理分类和品牌的名称
        handleCategoryAndBrandNames(spuList);
//      6   封装结果并返回
        PageInfo<Spu> info = new PageInfo<>(spuList);
        return new PageResult<>(info.getTotal(),spuList);
    }

    private void handleCategoryAndBrandNames(List<Spu> spuList) {
        for (Spu spu : spuList) {
//          设置分类名称
            String names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream()//把集合转为流
                    .map(Category::getName)//把Category对象流转为name的流
                    .collect(Collectors.joining("/"));//把流中的数据以/进行拼接
            spu.setCname(names);
//          设置品牌名称
            Brand brand = brandService.queryByid(spu.getBrandId());
            spu.setBname(brand.getName());
        }
    }

    @Override
    @Transactional
    public void saveGoods(Spu spu) {
//        1 新增spu
        spu.setId(null);//强制为null，因为主键是自增的
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        int count = spuMapper.insert(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_GOODS_ERROR);
        }
//        2 新增spuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.insert(spuDetail);
        if(count != 1){
            throw new LyException(ExceptionEnum.INSERT_GOODS_ERROR);
        }

        saveSkuAndStock(spu);
    }

    private void saveSkuAndStock(Spu spu) {
        int count;
        List<Sku> skus = spu.getSkus();
        //
        //
        // 3 新增sku
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count = skuMapper.insert(sku);//支持ID回显
            if(count != 1){
                throw new LyException(ExceptionEnum.INSERT_GOODS_ERROR);
            }
            // 4 新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            count = stockMapper.insert(stock);
            if(count != 1){
                throw new LyException(ExceptionEnum.INSERT_GOODS_ERROR);
            }
        }
//        int i = skuMapper.insertList(skus); 不支持ID回显，也就是插入之后，用skuId的时候还得查出来ID
    }

    @Override
    public SpuDetail queryDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail == null ){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spuDetail;
    }

    @Override
    public List<Sku> querySkuListBySpuId(Long spuId) {
//        查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skus)){
            throw new  LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
//        查询sku下的库存
        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if(stocks == null || stocks.size() < skus.size()){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
//        把stock的集合，变成一个map,key是skuId ,其值是stock
        Map<Long, Integer> map = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        for (Sku skus1 : skus) {
            skus1.setStock(map.get(skus1.getId()));
        }
        return skus;
    }

    @Override
    public void updateGoods(Spu spu) {
        Long spuId = spu.getId();
        if (spuId == null) {
            throw new LyException(ExceptionEnum.SPU_ID_CANNT_BE_NULL);
        }

//      1   查询原来有几个sku
        int count = 0;
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skuList)){
            //  删除  sku
            count = skuMapper.delete(sku);
            if(count != skuList.size()){
                throw new LyException(ExceptionEnum.UPDATE_GOODS_ERROR);
            }
//      2   删除 stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            count = stockMapper.deleteByIdList(ids);
            if(count != skuList.size()){
                throw new LyException(ExceptionEnum.UPDATE_GOODS_ERROR);
            }
        }

//      3   修改spu

//        有些字段不能改
        spu.setSaleable(null);
        spu.setValid(null);
        spu.setCreateTime(null);
        sku.setLastUpdateTime(new Date());
        count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count != 1){
            throw new LyException(ExceptionEnum.UPDATE_GOODS_ERROR);
        }
//      4   修改 spuDetail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count != 1){
            throw new LyException(ExceptionEnum.UPDATE_GOODS_ERROR);
        }
//      5   新增sku 和 stock
        saveSkuAndStock(spu);
    }
}
