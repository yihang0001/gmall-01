package com.atguigu.gmall.cart.pojo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class cart {

    private Long skuId;
    private Boolean check;
    private String image;
    private String title;
    private List<SkuSaleAttrValueEntity> saleAttrs;
    private BigDecimal price;
    private BigDecimal count;
    private List<ItemSaleVO> sales;
}
