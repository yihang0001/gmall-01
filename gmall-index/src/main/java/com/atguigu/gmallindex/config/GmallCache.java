package com.atguigu.gmallindex.config;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {
    /**
     * 前缀
     * @return
     */
    String prefix() default "";
    /**
     * 过期时间
     */
    int timeout() default 5;

    /**
     * 防止雪崩随机值范围
     * @return
     */
    int random() default 5;

    /**
     * 防击穿key
     */
    String lock() default "lock";
}
