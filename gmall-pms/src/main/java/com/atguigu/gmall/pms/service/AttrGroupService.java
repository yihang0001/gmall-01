package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author caiyihang
 * @email 55555@qq.com
 * @date 2020-02-18 20:33:45
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryGroupsByCid(QueryCondition queryCondition, Long cid);

    GroupVo queryGroupVoById(Long id);

    List<GroupVo> queryGroupWithAttrByCid(Long cid);

    List<ItemGroupVO> queryItemGroupsBySpuIdAndCid(Long spuId, Long cid);
}

