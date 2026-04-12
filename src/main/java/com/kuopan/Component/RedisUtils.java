package com.kuopan.Component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component("redisUtils")
@Slf4j
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    /**
     * Delete the cache
     *
     * @param key One value ore more
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public V get(String key) {
        if (key == null) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Put normal cache in
     *
     * @param key
     * @param value
     * @return true for success else @return false for failure
     */
    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Put Redis key:{}, value:{} failure", key, value);
            return false;
        }
    }

    /**
     * Put normal cache in and set timer
     */
    public boolean setT(String key, V value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Put Redis key:{}, value:{} failure", key, value);
            return false;
        }
    }
}
