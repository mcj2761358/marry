package com.tqmall.model;

import lombok.Data;

/**
 * Created by Minutch on 2015-05-31.
 */
@Data
public class Goods  implements PrimaryKeyInterface{
    private Long id;
    private String sn;
    private String name;
    private Double price;
    private String unit;
    private String ctime;
    private String des;

    @Override
    public Long getPrimaryKey() {
        return null;
    }
}
