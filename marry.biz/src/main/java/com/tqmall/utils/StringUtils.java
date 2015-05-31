package com.tqmall.utils;

/**
 * Created by Minutch on 2015-05-31.
 */
public class StringUtils {
    public static String makeGetMethod(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }
}
