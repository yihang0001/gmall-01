package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.SpuInfoDescService;
import com.atguigu.gmall.pms.vo.BaseAttrVo;
import com.atguigu.gmall.pms.vo.SkuInfoVo;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService descService;

    @Autowired
    private ProductAttrValueDao baseAttrDao;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesDao imagesDao;

    @Autowired
    private SkuSaleAttrValueDao saleAttrValueDao;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuByCidPage(QueryCondition queryCondition, Long cid) {
        
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        if(cid != 0){
            wrapper.eq("catalog_id",cid);
        }

        String key =queryCondition.getKey();

        if(StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.eq("id",key).or().like("spu_name",key));
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(queryCondition), wrapper);

        return new PageVo(page);
    }

    @GlobalTransactional
    @Override
    public void bigsave(SpuInfoVo spuInfoVo) {


         Long spuId =saveBaseAttrValue(spuInfoVo);

         //1-spu相关



        this.descService.saveSpuInfoDesc(spuInfoVo,spuId);


        //2-sku相关

        saveSku(spuInfoVo,spuId);


        this.amqpTemplate.convertAndSend("PMS-SPU-EXCHANGE","item.insert",spuId);
    }

    private void saveSku(SpuInfoVo spuInfoVo,Long spuId){

        List<SkuInfoVo> skus = spuInfoVo.getSkus();
        if(CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(skuInfoVo -> {

            skuInfoVo.setSpuId(spuId);
            skuInfoVo.setSkuCode(UUID.randomUUID().toString());
            skuInfoVo.setCatalogId(spuInfoVo.getCatalogId());
            skuInfoVo.setBrandId(spuInfoVo.getBrandId());
            List<String> images = skuInfoVo.getImages();
            if(!CollectionUtils.isEmpty(images)){
                skuInfoVo.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoVo.getSkuDefaultImg())?skuInfoVo.getSkuDefaultImg():images.get(0));
            }
            this.skuInfoDao.insert(skuInfoVo);
            Long skuId = skuInfoVo.getSkuId();

            if(!CollectionUtils.isEmpty(images)) {
                images.forEach(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setSkuId(skuId);
                    if (StringUtils.equals(image, skuInfoVo.getSkuDefaultImg())) {
                        skuImagesEntity.setDefaultImg(1);
                    } else {
                        skuImagesEntity.setDefaultImg(0);
                    }
                    this.imagesDao.insert(skuImagesEntity);
                });
            }


            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVo.getSaleAttrs();
            if(!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuSaleAttrValueEntity -> {
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    this.saleAttrValueDao.insert(skuSaleAttrValueEntity);
                });
            }


            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuInfoVo,skuSaleVo);
            this.smsClient.saveSkuSales(skuSaleVo);

        });

    }

    private Long saveBaseAttrValue(SpuInfoVo spuInfoVo){
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        Long spuId = spuInfoVo.getId();
        List<BaseAttrVo> baseAttrs = spuInfoVo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            baseAttrs.forEach(baseAttrVo -> {
                baseAttrVo.setSpuId(spuId);
                this.baseAttrDao.insert(baseAttrVo);
            });
        };
        return spuId;
    }

}