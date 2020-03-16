package com.atguigu.gmallindex.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmallindex.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

        MethodSignature Signature =(MethodSignature)joinPoint.getSignature();

        Method method = Signature.getMethod();
        GmallCache annotation = method.getAnnotation(GmallCache.class);

        String prefix = annotation.prefix();
        Class<?> returnType = method.getReturnType();

        Object[] args = joinPoint.getArgs();
        String param = Arrays.asList(args).toString();

        String json = this.redisTemplate.opsForValue().get(prefix + param);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseObject(json,returnType);
        }

        String lock = annotation.lock();
        RLock rLock = this.redissonClient.getLock(lock + param);
        rLock.lock();

        String json2 = this.redisTemplate.opsForValue().get(prefix + param);
        if(StringUtils.isNotBlank(json2)){
            rLock.unlock();
            return JSON.parseObject(json2,returnType);
        }

        Object result = joinPoint.proceed(joinPoint.getArgs());

        int timeout = annotation.timeout();
        int random = annotation.random();
        if(result != null){

        this.redisTemplate.opsForValue().set(prefix+param,JSON.toJSONString(result),timeout +new Random().nextInt(random), TimeUnit.MINUTES);
        }else{
        this.redisTemplate.opsForValue().set(prefix+param,JSON.toJSONString(result),timeout +new Random().nextInt(random), TimeUnit.SECONDS);
        }
        rLock.unlock();

        return result;
    }
}
