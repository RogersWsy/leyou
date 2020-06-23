package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecificationService {
    List<SpecGroup> queryGroupByCid(Long cid);

    int addGroup(SpecGroup specGroup);

    List<SpecParam> queryParam(Long gid, Long cid, Boolean searching);
}
