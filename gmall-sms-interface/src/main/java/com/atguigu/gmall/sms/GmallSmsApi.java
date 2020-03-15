package com.atguigu.gmall.sms;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallSmsApi {

    @PostMapping("sms/skubounds")
    public Resp<Object> saveSkuSales(@RequestBody SkuSaleVo skuSaleVo);

    @GetMapping("sms/skubounds/{skuId}")
    public Resp<List<ItemSaleVO>> queryItemSaleBySkuId(@PathVariable("skuId")Long skuId);

}
