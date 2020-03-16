package com.atguigu.gmall.gateway.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties("auth.jwt")
public class JwtProperties {

    private String publicKeyPath;
    private String cookieName;

    private PublicKey publicKey;

    @PostConstruct
    public void init(){
            try {

        this.publicKey = RsaUtils.getPublicKey(publicKeyPath);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("失去公钥!检查配置!");
            }
    }
}
