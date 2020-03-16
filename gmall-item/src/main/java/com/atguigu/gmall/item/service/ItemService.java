package com.atguigu.gmall.item.service;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVO queryItemBySkuId(long skuId) {
        ItemVO itemVO = new ItemVO();

        //sku信息
        CompletableFuture<SkuInfoEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.infosku(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {
                return null;
            }
            itemVO.setSkuId(skuId);
            itemVO.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVO.setSkuSubbTitle(skuInfoEntity.getSkuSubtitle());
            itemVO.setPrice(skuInfoEntity.getPrice());
            itemVO.setWeight(skuInfoEntity.getWeight());
            return skuInfoEntity;
        }, threadPoolExecutor);

        //营销信息
        CompletableFuture<Void> salesfuture = CompletableFuture.runAsync(()->{
        Resp<List<ItemSaleVO>> ltemSaleResp = this.smsClient.queryItemSaleBySkuId(skuId);
        List<ItemSaleVO> itemSaleVOList = ltemSaleResp.getData();
        itemVO.setSales(itemSaleVOList);
        },threadPoolExecutor);

        //库存信息
        CompletableFuture<Void> wmsfuture = CompletableFuture.runAsync(() -> {
            Resp<List<WareSkuEntity>> wareSkusBySkuId = this.wmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkusBySkuId.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
        }, threadPoolExecutor);

        //sku图片
        CompletableFuture<Void> imagefuture = CompletableFuture.runAsync(() -> {
            Resp<List<SkuImagesEntity>> skuImageBySkuId = this.pmsClient.querySkuImageBySkuId(skuId);
            List<SkuImagesEntity> skuImageBySkuIdData = skuImageBySkuId.getData();
            itemVO.setImages(skuImageBySkuIdData);
        }, threadPoolExecutor);

        //品牌信息
        CompletableFuture<Void> brandfuture = skuFuture.thenAcceptAsync(skuInfoEntity ->{
        Resp<BrandEntity> brandEntityResp = this.pmsClient.infobrand(skuInfoEntity.getBrandId());
        BrandEntity brandEntity = brandEntityResp.getData();
        if(brandEntity != null){
        itemVO.setBrandId(brandEntity.getBrandId());
        itemVO.setBrandName(brandEntity.getName());
        }
        },threadPoolExecutor);

        //分类信息
        CompletableFuture<Void> categoryfuture = skuFuture.thenAcceptAsync(skuInfoEntity ->{
        Resp<CategoryEntity> categoryEntityResp = this.pmsClient.info(skuInfoEntity.getCatalogId());
        CategoryEntity categoryEntity = categoryEntityResp.getData();
        if(categoryEntity != null){
        itemVO.setCategoryId(categoryEntity.getCatId());
        itemVO.setCategoryName(categoryEntity.getName());
        }
        },threadPoolExecutor);

        //spu信息
        CompletableFuture<Void> spufuture = skuFuture.thenAcceptAsync(skuInfoEntity ->{
        Long spuId = skuInfoEntity.getSpuId();
        Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsClient.infospu(spuId);
        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
        if(spuInfoEntity != null){
        itemVO.setSpuId(spuInfoEntity.getId());
        itemVO.setSpuName(spuInfoEntity.getSpuName());
        }
        },threadPoolExecutor);

        //销售属性
        CompletableFuture<Void> saleattrfuture = skuFuture.thenAcceptAsync(skuInfoEntity ->{
        Resp<List<SkuSaleAttrValueEntity>> saleAttrBySpuId = this.pmsClient.querySaleAttrBySpuId(skuInfoEntity.getSpuId());
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = saleAttrBySpuId.getData();
        itemVO.setSaleAttrs(skuSaleAttrValueEntities);
        },threadPoolExecutor);

        //组及组下参数
        CompletableFuture<Void> groupfuture = skuFuture.thenAcceptAsync(skuInfoEntity ->{
        Resp<List<ItemGroupVO>> listResp = this.pmsClient.queryItemGroupsBySpuIdAndCid(skuInfoEntity.getSpuId(), skuInfoEntity.getCatalogId());
        List<ItemGroupVO> itemGroupVOList = listResp.getData();
        itemVO.setGroups(itemGroupVOList);
        },threadPoolExecutor);


        //spu描述信息
        CompletableFuture<Void> spudescfuture = skuFuture.thenAcceptAsync(skuInfoEntity ->{
        Resp<SpuInfoDescEntity> infoDescEntityResp = this.pmsClient.querySpuInfoDesc(skuInfoEntity.getSpuId());
        SpuInfoDescEntity spuInfoDescEntity = infoDescEntityResp.getData();
        if(spuInfoDescEntity != null){
            String[] image = StringUtils.split(spuInfoDescEntity.getDecript(), ",");
            itemVO.setDesc(Arrays.asList(image));
        }
        },threadPoolExecutor);

        CompletableFuture.allOf(salesfuture,wmsfuture,imagefuture,brandfuture,categoryfuture,spufuture,saleattrfuture,groupfuture,spudescfuture).join();
        return itemVO;
    }
}
