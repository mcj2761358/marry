package com.tqmall.common.search;

import lombok.Data;

/**
 * 分页
 * Created by Minutch on 2015-06-01.
 */
@Data
public class PageParam {
    private Integer start = 0;
    private Integer limit = 10;
}
