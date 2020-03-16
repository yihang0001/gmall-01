package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.exception.MemberException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {

        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        switch (type){
            case 1:
                queryWrapper.eq("username",data); break;
            case 2:
                queryWrapper.eq("mobile",data);  break;
            case 3:
                queryWrapper.eq("email",data);  break;
            default:
                return null;
        }

        return this.count(queryWrapper) == 0;

    }

    @Override
    public Boolean regist(MemberEntity memberEntity, String code) {
        //验证码
        //this.redisTempelte.opsForValue.get();

        String salt = UUID.randomUUID().toString().substring(0, 5);
        memberEntity.setSalt(salt);

        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword()+salt));

        memberEntity.setCreateTime(new Date());
        memberEntity.setIntegration(1000);
        memberEntity.setGrowth(1000);
        memberEntity.setStatus(1);
        memberEntity.setLevelId(0l);
        this.save(memberEntity);

        //this.redisTempelte.opsForValue.get();
        return true;
    }

    @Override
    public MemberEntity queryUser(String username, String password) {

        MemberEntity user= this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));

        if(user == null){
            throw new MemberException("用户名输入有误");
        }

        password = DigestUtils.md5Hex(password + user.getSalt());

        if(!StringUtils.equals(password,user.getPassword())){
            throw new MemberException("密码错误");
        }

        return user;
    }

}