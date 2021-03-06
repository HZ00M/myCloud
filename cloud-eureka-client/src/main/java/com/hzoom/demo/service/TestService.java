package com.hzoom.demo.service;

import com.hzoom.core.redis.RedisDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestService {
    public static int redisNum ;

    @RedisDistributedLock(lockPreString = "test",retryNum = 3)
    public void redisNumIncrease(String key){
        redisNum++;
    }
}
