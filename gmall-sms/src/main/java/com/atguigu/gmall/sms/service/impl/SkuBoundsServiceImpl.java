package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVO;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionDao reductionDao;

    @Autowired
    private SkuLadderDao ladderDao;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveSkuSales(SkuSaleVo skuSaleVo) {

        Long skuId = skuSaleVo.getSkuId();

        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(skuId);
        skuBoundsEntity.setBuyBounds(skuSaleVo.getBuyBounds());
        skuBoundsEntity.setGrowBounds(skuSaleVo.getGrowBounds());
        List<Integer> work = skuSaleVo.getWork();
        if(!CollectionUtils.isEmpty(work)){
            skuBoundsEntity.setWork(work.get(3)*8+work.get(2)*4+work.get(1)*2+work.get(0)*1);
        }

        this.save(skuBoundsEntity);


        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        reductionEntity.setFullPrice(skuSaleVo.getFullPrice());
        reductionEntity.setReducePrice(skuSaleVo.getReducePrice());
        reductionEntity.setAddOther(skuSaleVo.getFullAaddOther());
        reductionEntity.setSkuId(skuId);
        this.reductionDao.insert(reductionEntity);


        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(skuId);
        ladderEntity.setFullCount(skuSaleVo.getFullCount());
        ladderEntity.setDiscount(skuSaleVo.getDiscount());
        ladderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        this.ladderDao.insert(ladderEntity);

    }

    @Override
    public List<ItemSaleVO> queryItemSaleBySkuId(Long skuId) {

        List<ItemSaleVO> itemSaleVOS = new ArrayList<>();

        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if(skuBoundsEntity != null){
            ItemSaleVO itemSaleVO = new ItemSaleVO();
            itemSaleVO.setType("积分");
            itemSaleVO.setDesc("成长积分送:"+skuBoundsEntity.getGrowBounds()+",购物积分送:"+skuBoundsEntity.getBuyBounds());
            itemSaleVOS.add(itemSaleVO);
        }

        SkuFullReductionEntity fullReductionEntity = this.reductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if(fullReductionEntity != null){
            ItemSaleVO itemSaleVO = new ItemSaleVO();
            itemSaleVO.setType("满减");
            itemSaleVO.setDesc("满"+fullReductionEntity.getFullPrice()+"减"+fullReductionEntity.getReducePrice());
            itemSaleVOS.add(itemSaleVO);
        }

        SkuLadderEntity ladderEntity = this.ladderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if(ladderEntity != null){
            ItemSaleVO itemSaleVO = new ItemSaleVO();
            itemSaleVO.setType("打折");
            itemSaleVO.setDesc("满"+ladderEntity.getFullCount()+"件,打"+ladderEntity.getDiscount().divide(new BigDecimal(10))+"折");
            itemSaleVOS.add(itemSaleVO);
        }
        return itemSaleVOS;
    }

}