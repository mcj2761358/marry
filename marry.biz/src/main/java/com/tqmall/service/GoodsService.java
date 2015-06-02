package com.tqmall.service;

import com.tqmall.common.BizContacts;
import com.tqmall.common.search.GoodsFieldsContacts;
import com.tqmall.common.search.GoodsSearchParam;
import com.tqmall.common.search.Sort;
import com.tqmall.index.GoodsFullIndex;
import com.tqmall.index.GoodsRealTimeIndex;
import com.tqmall.model.Goods;
import com.tqmall.utils.ESUtils;
import com.tqmall.utils.PPLUtils;
import com.tqmall.utils.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Minutch on 2015-06-01.
 */
public class GoodsService {

    /**
     * 全量创建索引
     * @return
     */
    public boolean fullIndex() {
        GoodsFullIndex goodsFullIndex = new GoodsFullIndex();
        boolean result = goodsFullIndex.makeFullIndex();
        return result;
    }

    /**
     * 启动实时索引
     */
    public void startRealIndex(){
        GoodsRealTimeIndex goodsRealTimeIndex = new GoodsRealTimeIndex();
        goodsRealTimeIndex.start(new Date().getTime());
    }

    /**
     * 搜索
     * @param goodsSearchParam
     * @return
     */
    public List<Goods> search(GoodsSearchParam goodsSearchParam) {

        SearchResponse searchResponse = executeQuery(goodsSearchParam);
        List<Goods> goodsList = makeQueryResult(searchResponse);
        return goodsList;
    }

    /**
     * 执行查询
     * @param goodsSearchParam
     * @return
     */
    private SearchResponse executeQuery(GoodsSearchParam goodsSearchParam) {

        SearchRequestBuilder searchRequestBuilder = ESUtils.getClient()
                .prepareSearch("shop")
                .setSearchType(SearchType.DEFAULT)
                .setExplain(true)
                .setFrom(goodsSearchParam.getStart())
                .setSize(goodsSearchParam.getLimit());
        makeVagueQueryCondition(goodsSearchParam,searchRequestBuilder);
        makeSortQueryCondition(goodsSearchParam,searchRequestBuilder);
        FilterBuilder andFilterBuilder = makeCommonQueryCondition(goodsSearchParam);
        searchRequestBuilder.setPostFilter(andFilterBuilder);
        searchRequestBuilder.addFields(GoodsFieldsContacts.RETURN_FIELD_NAMES);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        return searchResponse;
    }

    /**
     * 构造需要模糊匹配的字段
     * @param goodsSearchParam
     * @param searchRequestBuilder
     */
    public void makeVagueQueryCondition(GoodsSearchParam goodsSearchParam,SearchRequestBuilder searchRequestBuilder) {
        try {
            //需要模糊查询的字段，约定将从字段加Ppl的字段里查询
            for (String vagueField : GoodsFieldsContacts.VAGUE_FIELD_NAMES) {
                String methodName = StringUtils.makeGetMethod(vagueField);
                Method method = goodsSearchParam.getClass().getMethod(methodName);
                Object object = method.invoke(goodsSearchParam);
                if (object != null && object.toString().trim().length() > 0) {
                    //对该字段分词
                    List<String> queryList = PPLUtils.simplePPL(object.toString());
                    if (queryList != null && queryList.size() > 0) {
                        BoolQueryBuilder bqb = QueryBuilders.boolQuery();
                        for (String query : queryList) {
                            bqb.must(QueryBuilders.queryString(query).field(vagueField + BizContacts.SUFFIX_PPL));
                        }
                        searchRequestBuilder.setQuery(bqb);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * 按指定的字段排序
     * @param goodsSearchParam
     * @param searchRequestBuilder
     */
    public void makeSortQueryCondition(GoodsSearchParam goodsSearchParam,SearchRequestBuilder searchRequestBuilder) {
        List<Sort> sortList = goodsSearchParam.getSortList();
        if (sortList == null || sortList.size()==0) {
            return ;
        }

        for (Sort sort : sortList) {
            SortOrder sortOrder = "desc".equals(sort.getSort())?SortOrder.DESC:SortOrder.ASC;
            searchRequestBuilder.addSort(sort.getField(),sortOrder);
        }
    }

    /**
     * 公用查询条件
     * @param goodsSearchParam
     */
    public FilterBuilder makeCommonQueryCondition(GoodsSearchParam goodsSearchParam) {
        AndFilterBuilder andFilterBuilder = new AndFilterBuilder();
        try {
            for (String field : GoodsFieldsContacts.TERM_FIELD_NAMES) {
                Method method = goodsSearchParam.getClass().getMethod(StringUtils.makeGetMethod(field));
                Object object = method.invoke(goodsSearchParam);
                if (object != null) {
                    if (object instanceof List) {
                        List list = (List) object;
                        if (!list.isEmpty()) {
                            Object[] array = new Object[list.size()];
                            list.toArray(array);
                            andFilterBuilder.add(FilterBuilders.inFilter(field, array));
                        }
                    } else {
                        if (object !=null) {
                            andFilterBuilder.add(FilterBuilders.termFilter(field, object));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return andFilterBuilder;
    }


    /**
     * 构造返回结果
     * @param searchResponse
     * @return
     */
    public List<Goods> makeQueryResult(SearchResponse searchResponse) {
        List<Goods> goodsList = new ArrayList<Goods>();
        SearchHits searchHits = searchResponse.getHits();
        try {
            for (SearchHit searchHit : searchHits) {
                Map<String, SearchHitField> searchHitFieldMap = searchHit.getFields();
                Goods goods = new Goods();
                Field[] fields = goods.getClass().getDeclaredFields();
                for (Field field: fields ) {
                    String fieldName = field.getName();
                    if (searchHitFieldMap.get(fieldName) != null) {
                        Method method = goods.getClass().getMethod(StringUtils.makeSetMethod(fieldName),field.getType());
                        method.invoke(goods,searchHitFieldMap.get(fieldName).getValue());
                    }
                }
                goodsList.add(goods);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return goodsList;
    }
}
