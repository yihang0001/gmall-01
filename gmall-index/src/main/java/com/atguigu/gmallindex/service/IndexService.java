package com.atguigu.gmallindex.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.gmallindex.config.GmallCache;
import com.atguigu.gmallindex.feign.GmallPmsClient;
import jdk.internal.dynalink.beans.StaticClass;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:cates:";

    public List<CategoryEntity> queryLv1Category() {

        Resp<List<CategoryEntity>> categories = this.pmsClient.queryCategoriesByLevelOrPid(1, null);

        return categories.getData();
    }

    @GmallCache(prefix = "index:cates:", timeout = 14400 ,random = 300)
    public List<CategoryVo> queryLv2WithSubsByPid(Long pid) {

        Resp<List<CategoryVo>> listResp = this.pmsClient.queryCateGoryWithSubByPid(pid);
        List<CategoryVo> categoryVos = listResp.getData();

        return  categoryVos;
    }

    public List<CategoryVo> queryLv2WithSubsByPid222(Long pid) {

        String json = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseArray(json,CategoryVo.class);
        }

        RLock lock = this.redissonClient.getLock("lock" + pid);
        lock.lock();

        String json2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json2)){
            lock.unlock();
            return JSON.parseArray(json,CategoryVo.class);
        }

        Resp<List<CategoryVo>> listResp = this.pmsClient.queryCateGoryWithSubByPid(pid);
        List<CategoryVo> categoryVos = listResp.getData();

        if(!CollectionUtils.isEmpty(categoryVos)){
            this.redisTemplate.opsForValue().set(KEY_PREFIX +pid, JSON.toJSONString(categoryVos),3+(int)Math.random()*10, TimeUnit.DAYS);
        }else{
            this.redisTemplate.opsForValue().set(KEY_PREFIX +pid, JSON.toJSONString(categoryVos),3+(int)Math.random()*10, TimeUnit.MINUTES);
        }

        lock.unlock();
        return  categoryVos;
    }

    public void testlock(){

        String uuid = UUID.randomUUID().toString();

        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,30,TimeUnit.SECONDS);

        if(lock){
            String num = this.redisTemplate.opsForValue().get("num");
            if(StringUtils.isEmpty(num)){
                num = "0";
                this.redisTemplate.opsForValue().set("num",num);
            }
            int n = Integer.parseInt(num);
            this.redisTemplate.opsForValue().set("num",String.valueOf(++n));

//            if(StringUtils.equals(uuid,this.redisTemplate.opsForValue().get("lock"))){
//            this.redisTemplate.delete("lock");
//            }
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), uuid);

        }else{
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testlock();
        }

    }





}
