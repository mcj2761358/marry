package com.tqmall.model;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Minutch on 2015-05-31.
 */
@Data
public class Goods  implements PrimaryKeyInterface{
    private Integer id;
    private String sn;
    private String name;
    private Double price;
    private String unit;
    private String ctime;
    private String des;

    @Override
    public Integer getPrimaryKey() {
        return id;
    }

    public String[] getFieldNames(){

        List<String> fieldNames =new ArrayList<String>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fieldNames.add(field.getName());
            }
        }
        String fieldNameArr[] = new String[fieldNames.size()];
        for (int i=0;i<fieldNames.size();i++) {
            fieldNameArr[i] = fieldNames.get(i);
        }

        return fieldNameArr;
    }
}
