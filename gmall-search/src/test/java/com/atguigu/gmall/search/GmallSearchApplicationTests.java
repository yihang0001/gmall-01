package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepostiory;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchAttrVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GoodsRepostiory goodsRepostiory;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Test
    void contextLoads() {

        this.restTemplate.createIndex(GoodsVO.class);
        this.restTemplate.putMapping(GoodsVO.class);

        Long pageNum = Long.valueOf(1);
        Long pageSize = Long.valueOf(1001);

        do{


        QueryCondition condition =new QueryCondition();
        condition.setPage(pageNum);
        condition.setLimit(pageSize);
        Resp<List<SpuInfoEntity>> listResp = this.pmsClient.querySpuPage(condition);
        List<SpuInfoEntity> spuInfoEntities = listResp.getData();

        spuInfoEntities.forEach(spuInfoEntity -> {
            Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkuBySpuId(spuInfoEntity.getId());
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

        });

        pageSize = new Long(spuInfoEntities.size());
        pageNum++;

        } while (pageSize == 100);

    }

}
