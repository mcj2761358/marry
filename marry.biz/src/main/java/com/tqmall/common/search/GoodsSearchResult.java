package com.tqmall.common.search;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Minutch on 2015-06-01.
 */
@Data
public class GoodsSearchResult {
    private List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
}
