package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepostiory;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchAttrVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpuListener {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GoodsRepostiory goodsRepostiory;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search-item-queue",durable = "true",ignoreDeclarationExceptions = "true"),
            exchange = @Exchange(value = "PMS-SPU-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key ={"item.*"}
    ))
    public void listener(Long spuId){
        Resp<SpuInfoEntity> spuInfoEntityResp = pmsClient.infospu(spuId);
        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
        Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkuBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
        if(!CollectionUtils.isEmpty(skuInfoEntities)){

            List<GoodsVO> goodsVOS = skuInfoEntities.stream().map(skuInfoEntity -> {
                GoodsVO goodsVo = new GoodsVO();
                goodsVo.setSkuId(skuInfoEntity.getSkuId());
                goodsVo.setTitle(skuInfoEntity.getSkuTitle());

                goodsVo.setSale(null);
                goodsVo.setPic(skuInfoEntity.getSkuDefaultImg());
                goodsVo.setPrice(skuInfoEntity.getPrice().doubleValue());

                Long brandId = skuInfoEntity.getBrandId();
                Resp<BrandEntity> brandEntityResp = this.pmsClient.infobrand(brandId);
                BrandEntity brandEntity = brandEntityResp.getData();
                if(brandEntity !=null){
                    goodsVo.setBrandId(skuInfoEntity.getBrandId());
                    goodsVo.setBrandName(brandEntity.getName());
                }
                Long catalogId = skuInfoEntity.getCatalogId();
                Resp<CategoryEntity> categoryEntityResp = this.pmsClient.info(catalogId);
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if(categoryEntity !=null){
                    goodsVo.setCategoryId(skuInfoEntity.getCatalogId());
                    goodsVo.setCategoryName(categoryEntity.getName());
                }
                goodsVo.setCreateTime(spuInfoEntity.getCreateTime());

                Resp<List<WareSkuEntity>> wareSkuResp = this.wmsClient.queryWareSkusBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
                if(!CollectionUtils.isEmpty(wareSkuEntities)){
                    goodsVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                }
                Resp<List<ProductAttrValueEntity>> attrValueResp = this.pmsClient.queryAttrValueBySpuId(spuInfoEntity.getId());
                List<ProductAttrValueEntity> attrValueEntities = attrValueResp.getData();
                if(!CollectionUtils.isEmpty(attrValueEntities)){
                    List<SearchAttrVO> searchAttrVOS = attrValueEntities.stream().map(productAttrValueEntity -> {
                        SearchAttrVO searchAttrVO = new SearchAttrVO();
                        searchAttrVO.setAttrId(productAttrValueEntity.getAttrId());
                        searchAttrVO.setAttrName(productAttrValueEntity.getAttrName());
                        searchAttrVO.setAttrValue(productAttrValueEntity.getAttrValue());
                        return searchAttrVO;
                    }).collect(Collectors.toList());
                    goodsVo.setAttrs(searchAttrVOS);
                }
                return goodsVo;
            }).collect(Collectors.toList());


            this.goodsRepostiory.saveAll(goodsVOS);
        }
    }
}
