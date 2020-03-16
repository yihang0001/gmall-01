package com.atguigui.gmall.auth.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.MemberException;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigui.gmall.auth.config.JwtProperties;
import com.atguigui.gmall.auth.feign.GmallUmsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties({JwtProperties.class})
public class AuthService {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private GmallUmsClient umsClient;

    public String accredit(String userName, String password) {

        Resp<MemberEntity> memberEntityResp = this.umsClient.queryUser(userName, password);
        MemberEntity memberEntity = memberEntityResp.getData();
        if(memberEntity == null){
            throw new MemberException("用户名或密码错误");
        }

        try {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",memberEntity.getId());
        map.put("userName",memberEntity.getUsername());
        String token = JwtUtils.generateToken(map, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExprieTime());

        return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
