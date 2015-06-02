package com.tqmall.index;

import com.tqmall.common.BizContacts;
import com.tqmall.common.search.GoodsFieldsContacts;
import com.tqmall.dao.GoodsDao;
import com.tqmall.model.Goods;
import com.tqmall.utils.ESUtils;
import com.tqmall.utils.PPLUtils;
import com.tqmall.utils.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Minutch on 2015-05-31.
 */
public class GoodsFullIndex {

    //索引名称
    private String indexName = "shop";
    //类型名称
    private String typeName = "goods";
    //主键字段名称
    private String primaryKey = "id";

    private GoodsDao goodsDao = new GoodsDao();

    /**
     * 建全量索引
     */
    public boolean makeFullIndex() {

        //先删除已经存在的类型
        ESUtils.deleteType(indexName,typeName);

        //查询出所有数据
        List<Goods> goodsList = goodsDao.getAllData();

        //对所有数据建所有
        boolean result = bulkCreateIndex(goodsList);
        return result;
    }

    /**
     * 构建索引
     * @param goodsList
     */
    public boolean bulkCreateIndex(List<Goods> goodsList) {
        List<Map<String,Object>> documentList = makeDocs(goodsList);
        boolean result = ESUtils.processCreateIndex(indexName,typeName,primaryKey,documentList);
        return result;
    }

    /**
     * 批量构造索引文档
     * @param goodsList
     * @return
     */
    public List<Map<String,Object>> makeDocs(List<Goods> goodsList) {

        List<Map<String,Object>> documents = new ArrayList<Map<String, Object>>();
        for (Goods goods : goodsList) {
            Map<String,Object> document = makeDoc(goods);
            documents.add(document);
        }
        return documents;
    }

    /**
     * 构造索引文档
     * @param goods
     * @return
     */
    public Map<String,Object> makeDoc(Goods goods) {

        Map<String,Object> document = new HashMap<String, Object>();

        //使用反射获取对象的所有字段
        Field[] fieldArr = goods.getClass().getDeclaredFields();
        List<String> fieldNameList = new ArrayList<String>();
        for (Field field: fieldArr) {
            //如果不是static字段，将字段内容当如到文档中
            if (!Modifier.isStatic(field.getModifiers())) {
                String fieldName = field.getName();
                String fieldGetMethodName = StringUtils.makeGetMethod(fieldName);
                fieldNameList.add(fieldName);
                try {
                    Method method = goods.getClass().getMethod(fieldGetMethodName);
                    document.put(fieldName,method.invoke(goods));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //将需要分词的字段进行分词处理后加入到文档
        String[] vagueFields = GoodsFieldsContacts.VAGUE_FIELD_NAMES;
        try {
            for (String vagueField : vagueFields) {
                if (fieldNameList.contains(vagueField)) {
                    String methodName = StringUtils.makeGetMethod(vagueField);
                    Method method = goods.getClass().getMethod(methodName);
                    Object object = method.invoke(goods);
                    if (object != null) {
                        //分词后存到文档,以空格分隔单词
                        String value = StringUtils.makeListToStr(PPLUtils.simplePPL(object.toString()), " ");
                        document.put(vagueField + BizContacts.SUFFIX_PPL, value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return document;
    }

    /**
     * 删除索引
     * @param primaryKeys
     */
    public void bulkDeleteIndex(List<Integer> primaryKeys) {
        if (primaryKeys.isEmpty()) {
            return;
        }
        ESUtils.bulkDeleteIndex(indexName,typeName,primaryKeys);
    }

}


































