package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @PostMapping("pms/spuinfo/page")
    public Resp<List<SpuInfoEntity>> querySpuPage(@RequestBody QueryCondition queryCondition);

    @GetMapping("pms/skuinfo/{skuId}")
    public Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> infobrand(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> info(@PathVariable("catId") Long catId);

    @GetMapping("pms/category")
    public Resp<List<CategoryEntity>> queryCategoriesByLevelOrPid(@RequestParam(value = "level",defaultValue = "0")Integer level,
                                                                  @RequestParam(value = "parentCid",required = false)Long pid);

    @GetMapping("pms/category/{pid}")
    public Resp<List<CategoryVo>> queryCateGoryWithSubByPid(@PathVariable("pid")Long pid);

        @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> queryAttrValueBySpuId(@PathVariable("spuId")Long spuId);


    @GetMapping("pms/skuinfo/info/{skuId}")
    public Resp<SkuInfoEntity> infosku(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/spuinfo/info/{id}")
    public Resp<SpuInfoEntity> infospu(@PathVariable("id") Long id);

    @GetMapping("pms/spuimages/info/{id}")
    public Resp<SpuImagesEntity> infospuimage(@PathVariable("id") Long id);

    @GetMapping("withattrs/cat/{catId}")
    public Resp<List<GroupVo>> queryGroupWithAttrByCid(@PathVariable("catId")Long cid);

    @GetMapping("pms/skuimages/{skuId}")
    public Resp<List<SkuImagesEntity>> querySkuImageBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/skusaleattrvalue/{spuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySaleAttrBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/attrgroup/withattrvalues")
    public Resp<List<ItemGroupVO>> queryItemGroupsBySpuIdAndCid(@RequestParam("spuId")Long spuId,
                                                                @RequestParam("cid")Long cid);

    @GetMapping("pms/spuinfodesc/info/{spuId}")
    public Resp<SpuInfoDescEntity> querySpuInfoDesc(@PathVariable("spuId") Long spuId);
}
