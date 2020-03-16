package com.atguigui.gmall.auth.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.RSAUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties("auth.jwt")
public class JwtProperties {

    private String privateKeyPath;
    private String publicKeyPath;
    private String secret;
    private String cookieName;
    private Integer exprieTime;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init(){
            try {
        File prifile = new File(privateKeyPath);
        File pubfile = new File(publicKeyPath);
        if(!prifile.exists()||!pubfile.exists()){
                RsaUtils.generateKey(publicKeyPath,privateKeyPath,secret);
        }
        this.publicKey = RsaUtils.getPublicKey(publicKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(privateKeyPath);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("RSA失败");
            }
    }
}
