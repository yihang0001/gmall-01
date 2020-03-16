package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemVO {

    private Long categoryId;
    private String categoryName;

    private Long brandId;
    private String brandName;

    private Long spuId;
    private String spuName;

    private Long skuId;
    private String skuTitle;
    private String skuSubbTitle;
    private BigDecimal price;
    private BigDecimal weight;

    private List<ItemSaleVO> sales;

    private Boolean store =false;

    //sku的spu的所有sku营销信息
    private List<SkuSaleAttrValueEntity> saleAttrs;

    private List<SkuImagesEntity> images;

    private List<String> desc;

    //f分组及规格参数
    private List<ItemGroupVO> groups;
}
