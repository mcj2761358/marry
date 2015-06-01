package com.tqmall;

import com.tqmall.common.GoodsSearchParam;
import com.tqmall.common.Result;
import com.tqmall.index.GoodsFullIndex;
import com.tqmall.service.GoodsService;
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

        GoodsService goodsService = new GoodsService();
        boolean result = goodsService.index();
        return Result.wrapSuccessfulResult(result);
    }


    @RequestMapping("search")
    @ResponseBody
    public Result search(GoodsSearchParam searchParam) {



        return null;
    }

}
