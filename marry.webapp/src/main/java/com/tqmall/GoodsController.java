package com.tqmall;

import com.tqmall.common.Result;
import com.tqmall.index.GoodsFullIndex;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
 
/**
 * Created by Minutch on 2015-05-31.
 */
@Controller
@RequestMapping("goods")
public class GoodsController {

    @RequestMapping("index")
    @ResponseBody
    public Result index(){

        GoodsFullIndex goodsFullIndex = new GoodsFullIndex();
        goodsFullIndex.makeFullIndex();
        return Result.wrapSuccessfulResult("create index success");
    }
}
