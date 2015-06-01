package com.tqmall.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 分词工具
 * Created by Minutch on 2015-06-01.
 */
public class PPLUtils {

    /**
     * 全角半角特殊字符合集
     */
    private static List<String> specialCharList = null;
    /**
     * 数字字符
     */
    private static List<String> digitalCharList = new ArrayList<String>();
    /**
     * 字母
     */
    private static List<String> charsList = new ArrayList<String>();

    static {
        //构造特殊字符表
        String[] specialCharArr = {
                "`","~","!","@","#","$","%","^","&","(",")","-","_","+","=","\\","|","'","\"",";",":","/","?",".",">",",","<",
                "｀","～","！","@","＃","%","¥","&","％","……","＊","（","）","——","＋","＝","、","｜","‘","“","；","：","／","？","。","》","，","《"
        };
        specialCharList = Arrays.asList(specialCharArr);

        //构造数字列表
        for (int i=0; i<=9; i++) {
            digitalCharList.add(String.valueOf(i));
        }

        //构造字母列表
        for (int i=Integer.valueOf('a');i<=Integer.valueOf('z');i++) {
            charsList.add(String.valueOf((char)i));
        }
        for (int i=Integer.valueOf('A');i<=Integer.valueOf('Z');i++) {
            charsList.add(String.valueOf((char)i));
        }
        System.out.println();

    }

    /**
     * 简单分词，中文每个词都切分，英文按单词切分，特殊字符直接过滤
     * @return
     */
    public static List<String> simplePPL(String value){

        List<String> modeList = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();

        char[] charArr = value.toCharArray();
        for (int i=0; i<charArr.length;i++) {
            String charStr = String.valueOf(charArr[i]);
            int length = charStr.getBytes().length;

            //判断是否特殊字符，是的话直接跳过
            if (specialCharList.contains(charStr)) {
                continue;
            }

            //如果是空格
            if (" ".equals(charStr)) {
                //判断前面是否有未添加到List的英文单词或者数字
                if (sb.length() != 0) {
                    modeList.add(sb.toString());
                    sb.setLength(0);
                }
                continue;

            }
            //如果字节数为1，是数字或者英文字母
            if (length == 1) {
                sb.append(charStr);
            }
            //如果是汉字
            else if (length ==3) {
                //判断前面是否有未添加到List的英文单词或者数字
                if (sb.length() != 0) {
                    modeList.add(sb.toString());
                    sb.setLength(0);
                }
                modeList.add(charStr);
            }

        }
        return modeList;
    }


    public static void main(String[] args){
      System.out.println(simplePPL("我是中国人123@#@ asd sda#@＃＃！_ \b"));
    }
}
