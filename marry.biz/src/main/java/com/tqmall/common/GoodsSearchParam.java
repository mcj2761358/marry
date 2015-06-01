package com.tqmall.common;

import lombok.Data;

/**
 * 商品搜索参数
 * Created by Minutch on 2015-06-01.
 */
@Data
public class GoodsSearchParam {
    private String sn;
    private String name;
    private Double price;
    private String unit;
    private String ctime;
    private String des;
}
