package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author caiyihang
 * @email 55555@qq.com
 * @date 2020-03-09 03:27:45
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}