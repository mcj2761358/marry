package com.tqmall.service;

import com.tqmall.common.GoodsSearchParam;
import com.tqmall.index.GoodsFullIndex;
import com.tqmall.model.Goods;

import java.util.List;

/**
 * Created by Minutch on 2015-06-01.
 */
public class GoodsService {

    public boolean index() {
        GoodsFullIndex goodsFullIndex = new GoodsFullIndex();
        boolean result = goodsFullIndex.makeFullIndex();
        return result;
    }

    public List<Goods> search(GoodsSearchParam goodsSearchParam) {

        return null;
    }
}
