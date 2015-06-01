package com.tqmall.common.search;

/**
 * Created by Minutch on 2015-06-01.
 */
public class GoodsFieldsContacts {

    /**
     * 需要模块查询的字段
     */
    public static String[] VAGUE_FIELD_NAMES = {"name","des"};

    /**
     * 需要精确查询的字段
     */
    public static String[] TERM_FIELD_NAMES = {"id","sn","price","unit","ctime"};

    /**
     * 需要返回的字段
     */
    public static String[] RETURN_FIELD_NAMES = {"id","sn","price","unit","ctime","name","des"};
}
