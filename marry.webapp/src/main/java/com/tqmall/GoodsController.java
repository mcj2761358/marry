package com.tqmall;

import com.tqmall.common.search.GoodsSearchParam;
import com.tqmall.common.Result;
import com.tqmall.model.Goods;
import com.tqmall.service.GoodsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Minutch on 2015-05-31.
 */
@Controller
@RequestMapping("goods")
public class GoodsController {

    private GoodsService goodsService = new GoodsService();

    @RequestMapping("index")
    @ResponseBody
    public Result index(){

        boolean result = goodsService.fullIndex();
        return Result.wrapSuccessfulResult(result);
    }

    @RequestMapping("realIndex")
    @ResponseBody
    public Result realIndex() {
        goodsService.startRealIndex();
        return Result.wrapSuccessfulResult("start success!");
    }


    @RequestMapping("search")
    @ResponseBody
    public Result search(GoodsSearchParam searchParam) {

        List<Goods> goodsList = goodsService.search(searchParam);
        return Result.wrapSuccessfulResult(goodsList);
    }

}
