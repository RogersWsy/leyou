package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  ExceptionEnum {
    SPU_ID_CANNT_BE_NULL(400,"价格不能为空"),
    CATEGORY_NOT_FOUND(404,"商品分类不存在"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    SPEC_GROUP_NOT_FOUND(404,"规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404,"规格参数不存在"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    INSERT_BRAND_ERROR(500,"新增品牌失败"),
    INSERT_SPEC_GROUP_ERROR(500,"新增规格组失败"),
    INSERT_GOODS_ERROR(500,"新增商品失败"),
    UPDATE_GOODS_ERROR(500,"修改商品失败"),
    FILE_UPLOAD_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(500,"无效的文件类型"),
    NO_SEARCH_RESULT(404,"没有搜索到商品"),
    ;

    private int status;
    private String message;
}
