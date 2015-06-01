package com.tqmall.utils;

import java.util.List;

/**
 * Created by Minutch on 2015-05-31.
 */
public class StringUtils {

    /**
     * 构造实体类的Get方法
     * @param field
     * @return
     */
    public static String makeGetMethod(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    /**
     * 构造实体Set方法
     * @param field
     * @return
     */
    public static String makeSetMethod(String field) {
        return "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }
    public static String makeListToStr(List<String> modeList,String split) {
        StringBuffer resultBuffer = new StringBuffer();
        for (String str: modeList) {
            resultBuffer.append(str);
            resultBuffer.append(split);
        }
        return resultBuffer.toString().trim();
    }
}
