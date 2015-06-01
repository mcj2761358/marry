package com.tqmall.common.search;

import lombok.Data;

import java.util.List;

/**
 * 商品搜索参数
 * Created by Minutch on 2015-06-01.
 */
@Data
public class GoodsSearchParam extends PageParam {
    private Integer id;
    private String sn;
    private String name;
    private Double price;
    private String unit;
    private String ctime;
    private String des;
    private List<Sort> sortList;
}
